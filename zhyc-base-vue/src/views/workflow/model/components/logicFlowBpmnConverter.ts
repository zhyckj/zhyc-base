/*
 * Copyright (c) 2026 众汇云创科技（深圳）有限公司.
 * This file is part of ZHYC and is licensed for non-commercial use only.
 * Commercial use requires a separate written license from the copyright holder.
 * SPDX-License-Identifier: LicenseRef-ZHYC-NonCommercial
 */

import type LogicFlow from '@logicflow/core';

type LogicFlowGraphLike = LogicFlow.GraphConfigData | LogicFlow.GraphData;

type NodeKind = 'start' | 'approval' | 'condition' | 'end';
type AssigneeType = 'initiator' | 'user' | 'role' | 'post' | 'expression';
type ApprovalType = 'single' | 'all' | 'percent';

const WORKFLOW_NODE_TYPES: Record<NodeKind, string> = {
  start: 'workflow-start',
  approval: 'workflow-approval',
  condition: 'workflow-condition',
  end: 'workflow-end',
};

interface WorkflowNodeProperties {
  kind?: NodeKind;
  width?: number;
  height?: number;
  radius?: number;
  assigneeType?: AssigneeType;
  assigneeExpression?: string;
  candidateUserIds?: string[];
  candidateRoleCodes?: string[];
  candidatePostCodes?: string[];
  formKey?: string;
  approvalType?: ApprovalType;
  passCondition?: string;
  timeoutHours?: number;
  ccUserIds?: string[];
}

interface WorkflowEdgeProperties {
  conditionExpression?: string;
}

interface BpmnNode {
  id: string;
  rawId: string;
  kind: NodeKind;
  name: string;
  x: number;
  y: number;
  properties: WorkflowNodeProperties;
}

interface BpmnEdge {
  id: string;
  rawId: string;
  sourceRef: string;
  targetRef: string;
  name: string;
  conditionExpression: string;
  points: LogicFlow.Point[];
}

const DEFAULT_PROCESS_ID = 'Process';

/**
 * 创建审批流默认画布数据。
 *
 * @param modelCode 流程模型编码
 * @param modelName 流程模型名称
 * @returns LogicFlow 可渲染的默认节点和连线
 */
export function createDefaultLogicFlowData(
  modelCode: string,
  modelName: string,
): LogicFlow.GraphConfigData {
  const processId = normalizeBpmnId(modelCode, DEFAULT_PROCESS_ID);
  const approvalName = modelName?.trim() || '审批节点';
  return {
    nodes: [
      {
        id: `${processId}_Start`,
        type: WORKFLOW_NODE_TYPES.start,
        x: 120,
        y: 180,
        text: '开始节点',
        properties: { kind: 'start', width: 156, height: 56, radius: 14 },
      },
      {
        id: `${processId}_Approval`,
        type: WORKFLOW_NODE_TYPES.approval,
        x: 340,
        y: 180,
        text: approvalName,
        properties: { kind: 'approval', width: 196, height: 64, radius: 14 },
      },
      {
        id: `${processId}_End`,
        type: WORKFLOW_NODE_TYPES.end,
        x: 560,
        y: 180,
        text: '结束节点',
        properties: { kind: 'end', width: 156, height: 56, radius: 14 },
      },
    ],
    edges: [
      {
        id: `${processId}_Flow_Start_Approval`,
        type: 'polyline',
        sourceNodeId: `${processId}_Start`,
        targetNodeId: `${processId}_Approval`,
      },
      {
        id: `${processId}_Flow_Approval_End`,
        type: 'polyline',
        sourceNodeId: `${processId}_Approval`,
        targetNodeId: `${processId}_End`,
      },
    ],
  };
}

/**
 * 将已缓存的 BPMN XML 还原为 LogicFlow 图数据；解析失败时回退默认流程。
 *
 * @param xml BPMN XML 文本
 * @param modelCode 流程模型编码
 * @param modelName 流程模型名称
 * @returns LogicFlow 可渲染的节点和连线
 */
export function createLogicFlowDataFromBpmnXml(
  xml: string | undefined,
  modelCode: string,
  modelName: string,
): LogicFlow.GraphConfigData {
  if (!xml?.trim()) {
    return createDefaultLogicFlowData(modelCode, modelName);
  }
  try {
    const document = new DOMParser().parseFromString(xml, 'application/xml');
    if (getElementsByLocalName(document, 'parsererror').length > 0) {
      return createDefaultLogicFlowData(modelCode, modelName);
    }
    const shapeMap = collectBpmnShapes(document);
    const nodes = collectBpmnNodes(document, shapeMap);
    const edges = collectBpmnEdges(document);
    if (nodes.length === 0) {
      return createDefaultLogicFlowData(modelCode, modelName);
    }
    return { nodes, edges };
  } catch {
    return createDefaultLogicFlowData(modelCode, modelName);
  }
}

/**
 * 将 LogicFlow 图数据转换为 Flowable 可部署的 BPMN XML。
 *
 * @param graphData LogicFlow 当前画布数据
 * @param modelCode 流程模型编码
 * @param modelName 流程模型名称
 * @returns BPMN XML 字符串
 */
export function convertLogicFlowToBpmnXml(
  graphData: LogicFlowGraphLike,
  modelCode: string,
  modelName: string,
): string {
  const processId = normalizeBpmnId(modelCode, DEFAULT_PROCESS_ID);
  const nodes = normalizeNodes(graphData.nodes ?? []);
  const edges = normalizeEdges(graphData.edges ?? [], nodes);
  const nodeById = new Map(nodes.map((node) => [node.id, node]));
  const outgoingMap = groupEdges(edges, 'sourceRef');
  const incomingMap = groupEdges(edges, 'targetRef');
  const processElements = nodes.map((node) => {
    const incoming = incomingMap.get(node.id) ?? [];
    const outgoing = outgoingMap.get(node.id) ?? [];
    return renderProcessNode(node, incoming, outgoing);
  });
  const sequenceFlows = edges.map(renderSequenceFlow);
  const diagramElements = [
    ...nodes.map(renderBpmnShape),
    ...edges.map((edge) => renderBpmnEdge(edge, nodeById)),
  ];

  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" xmlns:flowable="http://flowable.org/bpmn" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" id="Definitions_${processId}" targetNamespace="http://zhyc.aieternal.cn/workflow">
  <bpmn:process id="${processId}" name="${escapeXml(modelName || modelCode || '流程模型')}" isExecutable="true">
${[...processElements, ...sequenceFlows].join('\n')}
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_${processId}">
    <bpmndi:BPMNPlane id="BPMNPlane_${processId}" bpmnElement="${processId}">
${diagramElements.join('\n')}
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;
}

function normalizeNodes(nodes: LogicFlow.NodeConfig[]): BpmnNode[] {
  return nodes.map((node, index) => {
    const kind = resolveNodeKind(node);
    return {
      id: normalizeBpmnId(node.id, `Node_${index + 1}`),
      rawId: String(node.id ?? `Node_${index + 1}`),
      kind,
      name: readText(node.text) || defaultNodeName(kind),
      x: Number(node.x) || 0,
      y: Number(node.y) || 0,
      properties: normalizeNodeProperties(kind, node.properties),
    };
  });
}

function collectBpmnShapes(document: Document): Map<string, { x: number; y: number }> {
  const shapeMap = new Map<string, { x: number; y: number }>();
  getElementsByLocalName(document, 'BPMNShape').forEach((shape) => {
    const bpmnElement = shape.getAttribute('bpmnElement');
    const bounds = getElementsByLocalName(shape, 'Bounds')[0];
    if (!bpmnElement || !bounds) {
      return;
    }
    const x = Number(bounds.getAttribute('x')) || 0;
    const y = Number(bounds.getAttribute('y')) || 0;
    const width = Number(bounds.getAttribute('width')) || 0;
    const height = Number(bounds.getAttribute('height')) || 0;
    shapeMap.set(bpmnElement, {
      x: x + width / 2,
      y: y + height / 2,
    });
  });
  return shapeMap;
}

function collectBpmnNodes(
  document: Document,
  shapeMap: Map<string, { x: number; y: number }>,
): LogicFlow.NodeConfig[] {
  const nodeNames = ['startEvent', 'userTask', 'exclusiveGateway', 'endEvent'];
  return nodeNames.flatMap((nodeName, groupIndex) =>
    getElementsByLocalName(document, nodeName).map((element, index) => {
      const id = element.getAttribute('id') || `${nodeName}_${index + 1}`;
      const kind = resolveBpmnElementKind(nodeName);
      const position = shapeMap.get(id) ?? {
        x: 120 + index * 180,
        y: 160 + groupIndex * 80,
      };
      return {
        id,
        type: resolveLogicFlowNodeType(kind),
        x: position.x,
        y: position.y,
        text: element.getAttribute('name') || defaultNodeName(kind),
        properties: {
          ...readBpmnNodeProperties(kind, element),
          ...resolveLogicFlowNodeSize(kind),
        },
      };
    }),
  );
}

function collectBpmnEdges(document: Document): LogicFlow.EdgeConfig[] {
  const edgeWaypoints = collectBpmnEdgeWaypoints(document);
  return getElementsByLocalName(document, 'sequenceFlow').map((element, index) => {
    const id = element.getAttribute('id') || `Flow_${index + 1}`;
    const conditionExpression = getElementsByLocalName(element, 'conditionExpression')[0]?.textContent?.trim();
    return {
      id,
      type: 'polyline',
      sourceNodeId: element.getAttribute('sourceRef') || '',
      targetNodeId: element.getAttribute('targetRef') || '',
      text: element.getAttribute('name') || '',
      pointsList: edgeWaypoints.get(id),
      properties: conditionExpression ? { conditionExpression } : {},
    };
  });
}

function collectBpmnEdgeWaypoints(document: Document): Map<string, LogicFlow.Point[]> {
  const waypointMap = new Map<string, LogicFlow.Point[]>();
  getElementsByLocalName(document, 'BPMNEdge').forEach((edge) => {
    const bpmnElement = edge.getAttribute('bpmnElement');
    if (!bpmnElement) {
      return;
    }
    const points = getElementsByLocalName(edge, 'waypoint').map((point) => ({
      x: Number(point.getAttribute('x')) || 0,
      y: Number(point.getAttribute('y')) || 0,
    }));
    waypointMap.set(bpmnElement, points);
  });
  return waypointMap;
}

function normalizeEdges(edges: LogicFlow.EdgeConfig[], nodes: BpmnNode[]): BpmnEdge[] {
  const nodeIds = new Set(nodes.map((node) => node.rawId));
  return edges
    .filter((edge) => nodeIds.has(edge.sourceNodeId) && nodeIds.has(edge.targetNodeId))
    .map((edge, index) => {
      const properties = (edge.properties ?? {}) as WorkflowEdgeProperties;
      const name = readText(edge.text);
      return {
        id: normalizeBpmnId(edge.id, `Flow_${index + 1}`),
        rawId: String(edge.id ?? `Flow_${index + 1}`),
        sourceRef: normalizeBpmnId(edge.sourceNodeId, `Source_${index + 1}`),
        targetRef: normalizeBpmnId(edge.targetNodeId, `Target_${index + 1}`),
        name,
        conditionExpression: properties.conditionExpression?.trim() || '',
        points: normalizeWaypoints(edge),
      };
    });
}

function resolveNodeKind(node: LogicFlow.NodeConfig): NodeKind {
  const properties = (node.properties ?? {}) as WorkflowNodeProperties;
  if (properties.kind) {
    return properties.kind;
  }
  if (node.type === 'circle' && readText(node.text).includes('结束')) {
    return 'end';
  }
  if (node.type === 'circle') {
    return 'start';
  }
  if (node.type === 'diamond') {
    return 'condition';
  }
  const nodeType = String(node.type);
  if (nodeType === WORKFLOW_NODE_TYPES.start) {
    return 'start';
  }
  if (nodeType === WORKFLOW_NODE_TYPES.condition) {
    return 'condition';
  }
  if (nodeType === WORKFLOW_NODE_TYPES.end) {
    return 'end';
  }
  return 'approval';
}

function normalizeNodeProperties(kind: NodeKind, properties?: LogicFlow.PropertiesType): WorkflowNodeProperties {
  const source = (properties ?? {}) as WorkflowNodeProperties;
  if (kind !== 'approval') {
    return { kind };
  }
  return {
    kind,
    assigneeType: source.assigneeType ?? 'initiator',
    assigneeExpression: source.assigneeExpression?.trim() || '',
    candidateUserIds: normalizeStringList(source.candidateUserIds),
    candidateRoleCodes: normalizeStringList(source.candidateRoleCodes),
    candidatePostCodes: normalizeStringList(source.candidatePostCodes),
    formKey: source.formKey?.trim() || '',
    approvalType: source.approvalType ?? 'single',
    passCondition: source.passCondition?.trim() || '',
    timeoutHours: normalizePositiveNumber(source.timeoutHours),
    ccUserIds: normalizeStringList(source.ccUserIds),
  };
}

function readBpmnNodeProperties(kind: NodeKind, element: Element): WorkflowNodeProperties {
  if (kind !== 'approval') {
    return { kind };
  }
  const assignee = readNamespacedAttribute(element, 'flowable', 'assignee');
  const candidateUsers = readNamespacedAttribute(element, 'flowable', 'candidateUsers');
  const candidateGroups = readNamespacedAttribute(element, 'flowable', 'candidateGroups');
  const formKey = readNamespacedAttribute(element, 'flowable', 'formKey');
  const dueDate = readNamespacedAttribute(element, 'flowable', 'dueDate');
  const groupCodes = splitCsv(candidateGroups);
  const candidateRoleCodes = groupCodes
    .filter((code) => code.startsWith('role:'))
    .map((code) => code.replace(/^role:/, ''));
  const candidatePostCodes = groupCodes
    .filter((code) => code.startsWith('post:'))
    .map((code) => code.replace(/^post:/, ''));
  return {
    kind,
    assigneeType: resolveAssigneeType(assignee, candidateUsers, candidateRoleCodes, candidatePostCodes),
    assigneeExpression: assignee && assignee !== '${initiator}' ? assignee : '',
    candidateUserIds: splitCsv(candidateUsers),
    candidateRoleCodes,
    candidatePostCodes,
    formKey: formKey ?? '',
    approvalType: (readWorkflowExtensionValue(element, 'approvalType') as ApprovalType) || 'single',
    passCondition: readWorkflowExtensionValue(element, 'passCondition'),
    timeoutHours: normalizePositiveNumber(readTimeoutHours(dueDate, element)),
    ccUserIds: splitCsv(readWorkflowExtensionValue(element, 'ccUserIds')),
  };
}

function renderFlowableUserTaskAttributes(properties: WorkflowNodeProperties): string {
  const attributes: string[] = [];
  if (properties.formKey) {
    attributes.push(`flowable:formKey="${escapeXml(properties.formKey)}"`);
  }
  if (properties.timeoutHours) {
    attributes.push(`flowable:dueDate="PT${properties.timeoutHours}H"`);
  }
  if (properties.assigneeType === 'user' && properties.candidateUserIds?.length === 1) {
    attributes.push(`flowable:assignee="${escapeXml(properties.candidateUserIds[0])}"`);
  } else if (properties.assigneeType === 'expression' && properties.assigneeExpression) {
    attributes.push(`flowable:assignee="${escapeXml(properties.assigneeExpression)}"`);
  } else if (properties.assigneeType === 'initiator') {
    attributes.push('flowable:assignee="${initiator}"');
  }
  if (properties.assigneeType === 'user' && (properties.candidateUserIds?.length ?? 0) > 1) {
    attributes.push(`flowable:candidateUsers="${escapeXml(properties.candidateUserIds?.join(',') ?? '')}"`);
  }
  const candidateGroups = buildCandidateGroups(properties);
  if (candidateGroups.length > 0) {
    attributes.push(`flowable:candidateGroups="${escapeXml(candidateGroups.join(','))}"`);
  }
  return attributes.length > 0 ? ` ${attributes.join(' ')}` : '';
}

function renderWorkflowExtensionElements(properties: WorkflowNodeProperties): string {
  const fields = [
    ['assigneeType', properties.assigneeType],
    ['approvalType', properties.approvalType],
    ['passCondition', properties.passCondition],
    ['timeoutHours', properties.timeoutHours ? String(properties.timeoutHours) : ''],
    ['ccUserIds', properties.ccUserIds?.join(',')],
  ].filter(([, value]) => value);
  if (fields.length === 0) {
    return '';
  }
  return `      <bpmn:extensionElements>
${fields.map(([name, value]) => renderFlowableField(name ?? '', value ?? '')).join('\n')}
      </bpmn:extensionElements>`;
}

function renderFlowableField(name: string, value: string): string {
  return `        <flowable:field name="${escapeXml(name)}">
          <flowable:string>${escapeXml(value)}</flowable:string>
        </flowable:field>`;
}

function renderProcessNode(node: BpmnNode, incoming: BpmnEdge[], outgoing: BpmnEdge[]): string {
  const tagName = getBpmnTagName(node.kind);
  const taskAttributes = node.kind === 'approval' ? renderFlowableUserTaskAttributes(node.properties) : '';
  const extensionElements = node.kind === 'approval' ? renderWorkflowExtensionElements(node.properties) : '';
  return `    <bpmn:${tagName} id="${node.id}" name="${escapeXml(node.name)}"${taskAttributes}>
${extensionElements}
${incoming.map((edge) => `      <bpmn:incoming>${edge.id}</bpmn:incoming>`).join('\n')}
${outgoing.map((edge) => `      <bpmn:outgoing>${edge.id}</bpmn:outgoing>`).join('\n')}
    </bpmn:${tagName}>`;
}

function renderSequenceFlow(edge: BpmnEdge): string {
  const nameAttribute = edge.name ? ` name="${escapeXml(edge.name)}"` : '';
  if (edge.conditionExpression) {
    return `    <bpmn:sequenceFlow id="${edge.id}"${nameAttribute} sourceRef="${edge.sourceRef}" targetRef="${edge.targetRef}">
      <bpmn:conditionExpression xsi:type="bpmn:tFormalExpression">${escapeXml(edge.conditionExpression)}</bpmn:conditionExpression>
    </bpmn:sequenceFlow>`;
  }
  return `    <bpmn:sequenceFlow id="${edge.id}"${nameAttribute} sourceRef="${edge.sourceRef}" targetRef="${edge.targetRef}" />`;
}

function renderBpmnShape(node: BpmnNode): string {
  const size = getNodeSize(node.kind);
  return `      <bpmndi:BPMNShape id="${node.id}_di" bpmnElement="${node.id}">
        <dc:Bounds x="${node.x - size.width / 2}" y="${node.y - size.height / 2}" width="${size.width}" height="${size.height}" />
      </bpmndi:BPMNShape>`;
}

function renderBpmnEdge(edge: BpmnEdge, nodeById: Map<string, BpmnNode>): string {
  const source = nodeById.get(edge.sourceRef) ?? { x: 0, y: 0 };
  const target = nodeById.get(edge.targetRef) ?? { x: 0, y: 0 };
  const points = edge.points.length > 0 ? edge.points : [source, target];
  return `      <bpmndi:BPMNEdge id="${edge.id}_di" bpmnElement="${edge.id}">
${points.map((point) => `        <di:waypoint x="${point.x}" y="${point.y}" />`).join('\n')}
      </bpmndi:BPMNEdge>`;
}

function normalizeWaypoints(edge: LogicFlow.EdgeConfig): LogicFlow.Point[] {
  if (Array.isArray(edge.pointsList) && edge.pointsList.length > 0) {
    return edge.pointsList.map((point) => ({
      x: Number(point.x) || 0,
      y: Number(point.y) || 0,
    }));
  }
  const points = [edge.startPoint, edge.endPoint].filter(Boolean) as LogicFlow.Point[];
  return points.map((point) => ({
    x: Number(point.x) || 0,
    y: Number(point.y) || 0,
  }));
}

function groupEdges(edges: BpmnEdge[], key: 'sourceRef' | 'targetRef'): Map<string, BpmnEdge[]> {
  return edges.reduce((map, edge) => {
    const bucket = map.get(edge[key]) ?? [];
    bucket.push(edge);
    map.set(edge[key], bucket);
    return map;
  }, new Map<string, BpmnEdge[]>());
}

function getBpmnTagName(kind: NodeKind): 'startEvent' | 'userTask' | 'exclusiveGateway' | 'endEvent' {
  if (kind === 'start') {
    return 'startEvent';
  }
  if (kind === 'end') {
    return 'endEvent';
  }
  if (kind === 'condition') {
    return 'exclusiveGateway';
  }
  return 'userTask';
}

function getNodeSize(kind: NodeKind): { width: number; height: number } {
  if (kind === 'approval') {
    return { width: 196, height: 64 };
  }
  if (kind === 'condition') {
    return { width: 172, height: 60 };
  }
  return { width: 156, height: 56 };
}

function resolveBpmnElementKind(localName: string): NodeKind {
  if (localName === 'startEvent') {
    return 'start';
  }
  if (localName === 'endEvent') {
    return 'end';
  }
  if (localName === 'exclusiveGateway') {
    return 'condition';
  }
  return 'approval';
}

function resolveLogicFlowNodeType(kind: NodeKind): string {
  return WORKFLOW_NODE_TYPES[kind];
}

function resolveLogicFlowNodeSize(kind: NodeKind): Record<string, number> {
  if (kind === 'approval') {
    return { width: 196, height: 64, radius: 14 };
  }
  if (kind === 'condition') {
    return { width: 172, height: 60, radius: 14 };
  }
  return { width: 156, height: 56, radius: 14 };
}

function defaultNodeName(kind: NodeKind): string {
  const names: Record<NodeKind, string> = {
    start: '开始节点',
    approval: '审批节点',
    condition: '条件节点',
    end: '结束节点',
  };
  return names[kind];
}

function getElementsByLocalName(root: Document | Element, localName: string): Element[] {
  return Array.from(root.getElementsByTagName('*')).filter((element) => element.localName === localName);
}

function buildCandidateGroups(properties: WorkflowNodeProperties): string[] {
  if (properties.assigneeType === 'role') {
    return normalizeStringList(properties.candidateRoleCodes).map((code) => `role:${code}`);
  }
  if (properties.assigneeType === 'post') {
    return normalizeStringList(properties.candidatePostCodes).map((code) => `post:${code}`);
  }
  return [];
}

function resolveAssigneeType(
  assignee: string | null,
  candidateUsers: string | null,
  candidateRoleCodes: string[],
  candidatePostCodes: string[],
): AssigneeType {
  if (candidateRoleCodes.length > 0) {
    return 'role';
  }
  if (candidatePostCodes.length > 0) {
    return 'post';
  }
  if (candidateUsers) {
    return 'user';
  }
  if (!assignee || assignee === '${initiator}') {
    return 'initiator';
  }
  if (/^\d+$/.test(assignee)) {
    return 'user';
  }
  return 'expression';
}

function readNamespacedAttribute(element: Element, namespace: string, name: string): string | null {
  return element.getAttribute(`${namespace}:${name}`) ?? element.getAttribute(name);
}

function readWorkflowExtensionValue(element: Element, fieldName: string): string {
  const field = getElementsByLocalName(element, 'field')
    .find((item) => item.getAttribute('name') === fieldName);
  if (!field) {
    return '';
  }
  const stringValue = getElementsByLocalName(field, 'string')[0]?.textContent;
  return stringValue?.trim() || '';
}

function readTimeoutHours(dueDate: string | null, element: Element): number | undefined {
  const extensionValue = readWorkflowExtensionValue(element, 'timeoutHours');
  if (extensionValue) {
    return normalizePositiveNumber(extensionValue);
  }
  const matched = dueDate?.match(/^PT(\d+)H$/);
  return matched ? Number(matched[1]) : undefined;
}

function splitCsv(value?: string | null): string[] {
  if (!value) {
    return [];
  }
  return value
    .split(',')
    .map((item) => item.trim())
    .filter(Boolean);
}

function normalizeStringList(value?: string[]): string[] {
  return Array.isArray(value) ? value.map((item) => String(item).trim()).filter(Boolean) : [];
}

function normalizePositiveNumber(value?: number | string): number | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  const numericValue = Number(value);
  if (!Number.isFinite(numericValue) || numericValue <= 0) {
    return undefined;
  }
  return Math.floor(numericValue);
}

function readText(text?: string | LogicFlow.TextConfig): string {
  if (!text) {
    return '';
  }
  if (typeof text === 'string') {
    return text.trim();
  }
  const legacyText = text as LogicFlow.TextConfig & { content?: string };
  return String(legacyText.value ?? legacyText.content ?? '').trim();
}

function normalizeBpmnId(value: unknown, fallback: string): string {
  const raw = String(value ?? fallback).trim() || fallback;
  const normalized = raw.replace(/[^A-Za-z0-9_.-]/g, '_');
  return /^[A-Za-z_]/.test(normalized) ? normalized : `Id_${normalized}`;
}

function escapeXml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&apos;');
}

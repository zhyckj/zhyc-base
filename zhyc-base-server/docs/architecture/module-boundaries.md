# Module Boundaries

- zhyc-common contains shared abstractions only.
- zhyc-auth-server owns OAuth2/OIDC and token issuing.
- zhyc-openapi-gateway owns API Key signature verification, API Key authentication, OAuth2 token verification, rate limiting, and call audit.
- zhyc-platform-app is the modular-monolith runtime.
- zhyc-module-system owns tenant, organization, user, role, menu, dictionary, parameter, audit, and monitoring.
- zhyc-module-lowcode owns data source modeling, table modeling, metadata, and code generation.
- zhyc-module-openapi owns developer applications, API Key management, OAuth2 client mapping, API catalog, authorization, and call audit.
- zhyc-module-workflow owns the platform workflow facade, runtime repository, todo task APIs, and approval records. Phase 1 ships `LocalWorkflowService` as a replaceable local implementation; later Flowable integration must remain behind `WorkflowService`.
- zhyc-module-purchase owns the purchase acceptance module, depends only on `WorkflowService` for approval flow startup, and exposes the first purchase request status Open API endpoint for integration verification.

## Dependency Rules

- zhyc-common carries shared contracts and stays lightweight.
- Business modules do not depend on each other directly.
- zhyc-platform-app composes the runtime.
- zhyc-module-openapi provides admin/developer-portal management APIs; zhyc-openapi-gateway consumes runtime credentials and enforces traffic security.
- zhyc-openapi-gateway must keep API Secret plaintext out of persistence; `JdbcApiKeyCredentialRepository` reads `openapi_api_key.secret_cipher` and delegates runtime secret resolution to `ApiSecretResolver`.
- zhyc-openapi-gateway checks runtime API authorization through `ApiPermissionRepository`; `JdbcApiPermissionRepository` reads enabled `openapi_api_permission` rows and matches method plus path pattern.
- zhyc-openapi-gateway writes runtime call audit through `ApiCallAuditRecorder`; `JdbcApiCallAuditRecorder` inserts `openapi_call_audit` rows for later portal and compliance queries.
- zhyc-openapi-gateway registers the API Key HTTP filter through `OpenApiGatewaySecurityConfig`; the filter protects `/openapi/*`, verifies API Key signatures, checks application API authorization, and writes call audit after the downstream chain returns.
- zhyc-openapi-gateway provides a default `ApiSecretResolver` bean only as a replaceable runtime extension point; production deployments must replace it with KMS, configuration center, or encryption-component backed secret resolution.
- zhyc-module-workflow provides `WorkflowService` as the only workflow contract visible to business modules; business modules must not import Flowable or other engine APIs directly.
- zhyc-module-workflow exposes `/workflow/tasks` for todo query, approve, and reject operations; these APIs are shared by the admin frontend and uni-app mobile approval entry.
- zhyc-module-purchase starts purchase approval through `WorkflowService.startProcess(...)` and persists the returned `process_instance_id` on `pur_request` for later task and audit correlation.

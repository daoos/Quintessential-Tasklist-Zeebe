# quintessential-tasklist-zeebe
The quintessential Zeebe tasklist for BPMN Human tasks with Drag and Drop Form builder, client and server side validations, and drop in Form Rendering

KOTLIN



## Workflow Linter

The workflow linter provides a linting/validation engine for BPMN workflows that are parsed by the Zeebe Model API.

The linter acts as a warning and error system allowing you to validate workflows during the modeling process, and you can 
implement the linter at deployment time, so when deploying a workflow into the Zeebe Cluster, the deployment will be stopped if a model has warnings or errors defined in the linter rules.

### Linter Rules

1. Rules are additive.  One rule cannot cancel out another rule.
1. Rules require a description, elementTypes, and a rule implementation.
1. Element Types: `ServiceTask`, `ReceiveTask`.... (more to come)
1. Rules can apply to multiple element types, and have various targeting rules.
1. the `Target` property defines "targeting rules" that applied to the rule.  Targeting rules define when the rule should be applied for the specific Element Type.
1. See the User Task example below for common usage: "Only target ServiceTasks with a task type of `user-task`.  This means the rule will only apply when a Service Task defines a type of `user-task` 

```yaml
orchestrator:
  workflow-linter:
    rules:
      global-rules:
        enable: false
        description: Global Restrictions for Service Task Types
        elementTypes:
          - ServiceTask
        serviceTaskRule:
          allowedTypes:
            - some-type
            - user-task

      user-task-rule:
        description: Specific rule for User Task Configuration of Service Tasks
        elementTypes:
          - ServiceTask
        target:
          serviceTasks:
            types:
              - user-task
        headerRule:
          requiredKeys:
            - title
            - candidateGroups
            - formKey
          allowedNonDefinedKeys: false
          allowedDuplicateKeys: false
          optionalKeys:
            - priority
            - assignee
            - candidateUsers
            - dueDate
            - description
```


Responses can be of a WARNING or ERROR

```
Element ---> serviceTask ServiceTask_1luzsfd
Type: ERROR
Code: 0
Element Type: serviceTask
Element Id: ServiceTask_1luzsfd
Message: Missing Required Headers: [title, candidateGroups, formKey]

Type: ERROR
Code: 0
Element Type: serviceTask
Element Id: ServiceTask_1luzsfd
Message: Found headers that are not part of Optional Headers list: [priority, assignee, candidateUsers, dueDate, description]
```

### Global Rules

If the `target` property configuration is **not** provided, then the rule will be applied globally to all Element Types defined in the rule.

Global rules can be a good way to implement some restrictions on your modeling teams to ensure that internal types and correlation keys are not used.

Global rules can be valuable naming conventions as well: "task types cannot start with a underscore `_`."

### Element Type Rules (WIP)

Working model is to have rule factories for each major Element Type (Service Task, Receive Task, Catch message, etc).

Element Type Rules provide specific rule implementations that focus on the Element Type's configuration possibilities:

1. Service Task:
   1. Allowed Types + regex limit
   1. Allowed Retry Regex
   1. Allowed IO Mappings
   1. Allowed Headers (Required, Optional, duplicates, Key Value Pairs, Non-Defined Keys, etc)
1. Receive Task:
   1. Allowed Correlation Keys
   1. Correlation Key Restrictions (limit names allowed to be used + regex limit)
   1. Out-mapping restrictions.


### Formatting Rules (WIP)

The Linter is not just about execution implementation rules, you can also define formatting rules for the BPMN.

Formatting rules enable you to prevent common errors in formatting of BPMN.

1. Label all Gateways
1. Pool Usage
1. Prevent label patterns with Regex
1. Gateways labels end in "?"
1. No Expressions
1. Double sequence flows
1. .. More?


### TODO

1. Add support to define what will prevent a deployment (WARNING / ERRORS)
1. Add optional parameter on deployment endpoint to validate using linter
1. Provide JSON error support
1. Provide Language Server implementation of linter.
1. Add IO Mappings rules
1. Add Receive Task Rule
1. Add Message Rules
1. Add Formatting rules
1. Add Timer rules to prevent certain spectrum of timer durations / cycles
1. Add targeting based on BPMN Process Key
1. Add special negating rule for Task Types and Correlation Keys
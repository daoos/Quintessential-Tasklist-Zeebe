package com.github.stephenott.qtz.tasks.worker

import com.github.stephenott.qtz.tasks.domain.UserTaskEntity
import com.github.stephenott.qtz.tasks.domain.UserTaskState
import com.github.stephenott.qtz.tasks.domain.ZeebeVariables
import com.github.stephenott.qtz.tasks.repository.UserTasksRepository
import com.github.stephenott.qtz.zeebe.management.ZeebeManagementClientConfiguration
import com.github.stephenott.qtz.zeebe.management.repository.ZeebeManagementRepository
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.zeebe.client.api.response.ActivatedJob
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface JobProcessor {
    fun processJob(job: ActivatedJob): Single<UserTaskEntity>
}

@Singleton
class UserTaskZeebeJobProcessor: JobProcessor {

    @Inject
    private lateinit var userTaskRepository: UserTasksRepository

    @Inject
    private lateinit var zClientConfig: ZeebeManagementClientConfiguration

    override fun processJob(job: ActivatedJob): Single<UserTaskEntity> {
        return Single.fromCallable {
            println("Processing Job...")
            Thread.sleep(10000)
            val entity = zeebeJobToUserTaskEntity(job, this.zClientConfig)
            userTaskRepository.save(entity)
                    .subscribeOn(Schedulers.io())
                    .doOnSuccess { ut ->
                        println("User Task was captured from Zeebe and saved: ${ut.taskId}")
                    }.blockingGet()
        }

    }

    private fun zeebeJobToUserTaskEntity(job: ActivatedJob, config: ZeebeManagementClientConfiguration): UserTaskEntity {
        return UserTaskEntity(
                state = UserTaskState.NEW,
                taskOriginalCapture = Instant.now(),
                title = job.customHeaders["title"]
                        ?: throw IllegalArgumentException("Missing task title configuration"),
                description = job.customHeaders["description"],
                priority = job.customHeaders["priority"]?.toInt() ?: 0,
                assignee = job.customHeaders["assignee"],
                candidateGroups = job.customHeaders["candidateGroups"]?.split(",")?.toSet(),
                candidateUsers = job.customHeaders["candidateGroups"]?.split(",")?.toSet(),
//                dueDate = Instant.parse(job.customHeaders["dueDate"]) ?: null,
                dueDate = null,
                formKey = job.customHeaders["formKey"]
                        ?: throw IllegalArgumentException("formKey is missing."),
                zeebeJobKey = job.key,
                zeebeVariablesAtCapture = ZeebeVariables(job.variablesAsMap),
                zeebeSource = config.clusterName,
                zeebeBpmnProcessId = job.bpmnProcessId,
                zeebeBpmnProcessVersion = job.workflowDefinitionVersion,
                zeebeBpmnProcessKey = job.workflowKey,
                zeebeElementInstanceKey = job.elementInstanceKey,
                zeebeElementId = job.elementId,
                zeebeJobDealine = Instant.ofEpochMilli(job.deadline),
                zeebeJobRetriesRemaining = job.retries)
    }

}
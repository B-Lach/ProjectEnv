package com.bredogen.projectenv.extensions

import com.bredogen.projectenv.services.ProjectEnvService
import com.intellij.ide.DataManager
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys
import com.intellij.openapi.projectRoots.Sdk
import org.jetbrains.sbt.project.SbtEnvironmentVariablesProvider

import scala.collection.immutable.Map
import scala.jdk.CollectionConverters

class SbtTaskEnvironmentVariablesProvider : SbtEnvironmentVariablesProvider {
    override fun getAdditionalVariables(sdk: Sdk?): Map<String, String> {
        val values = linkedMapOf<String, String>()

        try {
            val context = DataManager.getInstance().dataContextFromFocusAsync.blockingGet(1000)
            val project = context?.getData(PlatformCoreDataKeys.PROJECT)

            if (project != null) {
                val envService = ProjectEnvService.getInstance(project)
                values.putAll(envService.getEnvValues())
            }
        } catch (ex: Exception) {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("ProjectEnv")
                    .createNotification(
                            "Can not read env variables for sbt project: ${ex.message}",
                            NotificationType.WARNING)
                    .notify(null)
        }
        return toScalaMap(values)
        // return toScalaMap(emptyMap())
    }

    private fun toScalaMap(map: kotlin.collections.Map<String, String>): Map<String, String> {
        return Map.from(CollectionConverters.MapHasAsScala(map).asScala())
    }
}
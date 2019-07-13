/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.multitier

import loci._
import org.apache.flink.multitier._

import org.apache.flink.runtime.executiongraph.ExecutionAttemptID
import org.apache.flink.runtime.taskmanager
import org.apache.flink.runtime.taskmanager.TaskExecutionState

@multitier trait TaskManagerActions {
  @peer type TaskManager <: { type Tie <: Single[TaskManager] }

  def notifyFinalState(executionAttemptID: ExecutionAttemptID): Unit on TaskManager

  def notifyFatalError(message: String, cause: Throwable): Unit on TaskManager

  def failTask(executionAttemptID: ExecutionAttemptID, cause: Throwable): Unit on TaskManager

  def updateTaskExecutionState(taskExecutionState: TaskExecutionState): Unit on TaskManager

  val taskManagerActions = on[TaskManager] local { implicit! =>
    new taskmanager.TaskManagerActions {
      def notifyFinalState(executionAttemptID: ExecutionAttemptID) =
        remote call TaskManagerActions.this.notifyFinalState(executionAttemptID)

      def notifyFatalError(message: String, cause: Throwable) =
        remote call TaskManagerActions.this.notifyFatalError(message, cause)

      def failTask(executionAttemptID: ExecutionAttemptID, cause: Throwable) =
        remote call TaskManagerActions.this.failTask(executionAttemptID, cause)

      def updateTaskExecutionState(taskExecutionState: TaskExecutionState) =
        remote call TaskManagerActions.this.updateTaskExecutionState(taskExecutionState)
    }
  }
}

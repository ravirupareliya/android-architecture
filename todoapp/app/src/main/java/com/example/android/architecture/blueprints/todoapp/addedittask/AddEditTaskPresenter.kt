/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.architecture.blueprints.todoapp.addedittask

import com.example.android.architecture.blueprints.todoapp.data.Task
import com.example.android.architecture.blueprints.todoapp.data.source.TasksDataSource

/**
 * Listens to user actions from the UI ([AddEditTaskFragment]), retrieves the data and updates
 * the UI as required.
 */
class AddEditTaskPresenter
/**
 * Creates a presenter for the add/edit view.

 * @param mTaskId ID of the task to edit or null for a new task
 * *
 * @param mTasksRepository a repository of data for tasks
 * *
 * @param mAddTaskView the add/edit view
 * *
 * @param shouldLoadDataFromRepo whether data needs to be loaded or not (for config changes)
 */
(private val mTaskId: String?, private val mTasksRepository: TasksDataSource, private val mAddTaskView: AddEditTaskContract.View,
 private var shouldLoadDataFromRepo: Boolean) : AddEditTaskContract.Presenter, TasksDataSource.GetTaskCallback {

    override fun start() {
        if (mTaskId != null && shouldLoadDataFromRepo) {
            populateTask()
        }
    }

    override fun saveTask(title: String, description: String) = when (mTaskId) {
        null -> createTask(title, description)
        else -> updateTask(title, description)
    }

    override fun populateTask() {
        if (mTaskId == null) {
            throw RuntimeException("populateTask() was called but task is new.")
        }
        mTasksRepository.getTask(mTaskId, this)
    }

    override fun onTaskLoaded(task: Task) {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.setTitle(task.title)
            mAddTaskView.setDescription(task.description)
        }
        shouldLoadDataFromRepo = false
    }

    override fun onDataNotAvailable() {
        // The view may not be able to handle UI updates anymore
        if (mAddTaskView.isActive()) {
            mAddTaskView.showEmptyTaskError()
        }
    }

    override fun isDataMissing(): Boolean = shouldLoadDataFromRepo

    private fun createTask(title: String, description: String) {
        val newTask = Task(title, description)
        if (newTask.isEmpty) {
            mAddTaskView.showEmptyTaskError()
        } else {
            mTasksRepository.saveTask(newTask)
            mAddTaskView.showTasksList()
        }
    }

    private fun updateTask(title: String, description: String) {
        if (mTaskId == null) {
            throw RuntimeException("updateTask() was called but task is new.")
        }
        mTasksRepository.saveTask(Task(title, description, mTaskId))
        mAddTaskView.showTasksList() // After an edit, go back to the list.
    }
}

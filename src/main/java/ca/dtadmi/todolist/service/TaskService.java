package ca.dtadmi.todolist.service;

import ca.dtadmi.todolist.dao.TaskDao;
import ca.dtadmi.todolist.entity.TaskEntity;
import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import ca.dtadmi.todolist.model.Task;
import com.google.api.client.util.DateTime;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService {

    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    public List<Task> findAll() {
        return taskDao.getAll().stream().map(MarshallService::unSerialize).filter(Objects::nonNull).toList();
    }

    public List<Task> findAllFromUser(String userId) {
        return taskDao.getAll().stream().map(MarshallService::unSerialize).filter(Objects::nonNull).filter(task -> task.getUserId().equals(userId)).toList();
    }

    public Task findById(String id) {
        return MarshallService.unSerialize(taskDao.get(id).orElse(null));
    }

    public Task findByName(String name) {
        return taskDao.getAll().stream().map(MarshallService::unSerialize).filter(Objects::nonNull).filter(task -> task.getName().equals(name)).findFirst().orElse(null);
    }

    public Task create(TaskEntity newTaskEntity) {
        String now = new DateTime(new Date()).toString();
        newTaskEntity.setCreationDate(now);
        newTaskEntity.setLastModificationDate(now);
        return MarshallService.unSerialize(taskDao.save(newTaskEntity));
    }

    public Task update(Task taskEntityUpdate) {
        TaskEntity currentTaskEntity = taskDao.get(taskEntityUpdate.getId()).orElse(null);
        if(currentTaskEntity != null) {
            taskEntityUpdate.setCreationDate(new DateTime(currentTaskEntity.getCreationDate()));
            taskEntityUpdate.setUserId(currentTaskEntity.getUserId());
            taskEntityUpdate.setLastModificationDate(new DateTime(new Date()));
            TaskEntity dataUpdate = new TaskEntity(MarshallService.serialize(taskEntityUpdate));
            return MarshallService.unSerialize(taskDao.update(dataUpdate).orElse(null));
        }
        return null;
    }

    public Task updateSubtasks(Task taskEntityUpdate) {
        TaskEntity currentTaskEntity = taskDao.get(taskEntityUpdate.getId()).orElse(null);
        if(currentTaskEntity != null) {
            taskEntityUpdate.setCreationDate(new DateTime(currentTaskEntity.getCreationDate()));
            taskEntityUpdate.setUserId(currentTaskEntity.getUserId());
            taskEntityUpdate.setLastModificationDate(new DateTime(new Date()));
            TaskEntity dataUpdate = new TaskEntity(MarshallService.serialize(taskEntityUpdate));
            TaskEntity updatedTaskEntity;
            if(dataUpdate.getSubtasks()!=null){
                List<String> subtasks = dataUpdate.getSubtasks();
                if(subtasks.stream().allMatch(subtaskId -> taskDao.get(subtaskId).isPresent())) {
                    updatedTaskEntity = taskDao.updateField(dataUpdate.getId(), "subtasks", subtasks);
                    subtasks.forEach(subtaskId -> taskDao.updateField(subtaskId, "superTask", updatedTaskEntity.getId()));
                } else {
                    throw new FirestoreExcecutionException("Some of the field values passed to be updated do not exist in database", HttpStatus.SC_BAD_REQUEST);
                }
            } else {
                updatedTaskEntity = taskDao.updateField(dataUpdate.getId(), "subtasks", null);
                List<String> subtasks = currentTaskEntity.getSubtasks();
                if(subtasks!=null && (subtasks.stream().allMatch(subtaskId -> taskDao.get(subtaskId).isPresent()))) {
                        subtasks.forEach(subtaskId -> taskDao.updateField(subtaskId, "superTask", null));
                }
            }

            return MarshallService.unSerialize(updatedTaskEntity);
        }
        return null;
    }

    public void removeAll() {
        taskDao.deleteAll();
    }

    public void remove(String id) {
        TaskEntity taskEntity = taskDao.get(id).orElseThrow(NoSuchElementException::new);
        List<String> subtasks = taskEntity.getSubtasks();
        if(subtasks != null) {
            taskEntity.getSubtasks().forEach(subtaskId -> {
                if(taskDao.get(subtaskId).isPresent()) {
                    taskDao.delete(subtaskId);
                }
            });
        }
        if(taskEntity.getSuperTask()!= null && !taskEntity.getSuperTask().isBlank()){
            TaskEntity superTaskEntity = taskDao.get(taskEntity.getSuperTask()).orElse(null);
            if(superTaskEntity != null){
                subtasks = superTaskEntity.getSubtasks().stream().filter(subtaskId -> !Objects.equals(subtaskId, taskEntity.getId())).toList();
                superTaskEntity.setSubtasks(subtasks);
                taskDao.update(superTaskEntity);
            }
        }
        taskDao.delete(id);

    }

}

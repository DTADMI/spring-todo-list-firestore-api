package ca.dtadmi.todolist.service;

import ca.dtadmi.todolist.dao.TaskDao;
import ca.dtadmi.todolist.entity.BaseTask;
import ca.dtadmi.todolist.entity.Task;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskDao taskDao;

    public TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }
    /**
     * TODO :
     * implement cache
     * implement rate limiting
     * */

    public List<Task> findAll() {
        return taskDao.getAll().stream().toList();
    }

    public List<Task> findAllFromUser(String userId) {
        return taskDao.getAll().stream().filter(task -> task.getUserId().equals(userId)).toList();
        /*try {
            // asynchronously retrieve multiple documents
            ApiFuture<QuerySnapshot> future = FirebaseInitialization.getTaskCollection().whereEqualTo("userId", userId).get();
// future.get() blocks on response
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents.stream().map((DocumentSnapshot document) -> {
                Task task = document.toObject(Task.class);
                task.setId(document.getId());
                return task;
            }).toList();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }

    public Task findById(String id) {
        return taskDao.get(id).orElse(null);
    }

    public Task findByName(String name) {
        return taskDao.getAll().stream().filter(task -> task.getName().equals(name)).findFirst().orElse(null);
        /*try {
            // asynchronously retrieve multiple documents
            ApiFuture<QuerySnapshot> future = FirebaseInitialization.getTaskCollection().whereEqualTo("name", name).get();
// future.get() blocks on response
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents.stream().map((DocumentSnapshot document) -> {
                Task task = document.toObject(Task.class);
                task.setId(document.getId());
                return task;
            }).findFirst().orElse(null);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }

    public Task create(BaseTask newTask) {
        return taskDao.save((Task) newTask);
    }

    public Task update(Task taskUpdate) {
        return taskDao.update(taskUpdate).orElse(null);
    }

    public Task updateSubtasks(Task taskUpdate) {
        Task dataUpdate = new Task();
        dataUpdate.setSubtasks(taskUpdate.getSubtasks() != null ? new ArrayList<>(taskUpdate.getSubtasks()) : new ArrayList<>());
        dataUpdate.setLastModificationDate(new Date().toString());
        Task updatedTask = taskDao.update(dataUpdate).orElse(null);
        if(updatedTask != null){
            dataUpdate.getSubtasks().forEach(subtaskId ->{
                Task subtaskDataUpdate = new Task();
                subtaskDataUpdate.setSuperTask(taskUpdate.getId());
                subtaskDataUpdate.setLastModificationDate(new Date().toString());
                taskDao.update(subtaskDataUpdate).orElse(null);
            });
        }
        return updatedTask;
        /*try {
            Task dataUpdate = new Task();
            dataUpdate.setSubtasks(taskUpdate.getSubtasks() != null ? new ArrayList<>(taskUpdate.getSubtasks()) : new ArrayList<>());
            dataUpdate.setLastModificationDate(new Date().toString());
            // asynchronously update doc, create the document if missing
            DocumentReference docReference = FirebaseInitialization.getTaskCollection().document(dataUpdate.getId());
            ApiFuture<WriteResult> writeResult =
                    docReference.set(dataUpdate, SetOptions.merge());
            // ...
            logger.debug("Update time : {}", writeResult.get().getUpdateTime());

            dataUpdate.getSubtasks().forEach(subtaskId ->{
              DocumentReference subtaskDocRef = FirebaseInitialization.getTaskCollection().document(subtaskId);
              Task subtaskDataUpdate = new Task();
              subtaskDataUpdate.setSuperTask(taskUpdate.getId());
              subtaskDataUpdate.setLastModificationDate(new Date().toString());
              subtaskDocRef.set(subtaskDataUpdate, SetOptions.merge());
            });

            // future.get() blocks on response
            DocumentSnapshot document = docReference.get().get();
            if (document.exists()) {
                logger.debug("Document data: {}", document.getData());
                return (Task) document.getData();
            } else {
                logger.debug("No such document!");
                return null;
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }*/
    }

    public void remove(String id) {
        try{
            Task task = taskDao.get(id).get();
            task.getSubtasks().forEach(taskDao::delete);
            if(task.getSuperTask()!= null && !task.getSuperTask().isBlank()){
                Task superTask = taskDao.get(task.getSuperTask()).orElse(null);
                if(superTask != null){
                    List<String> subtasks = superTask.getSubtasks().stream().filter(subtaskId -> subtaskId != task.getId()).collect(Collectors.toList());
                    superTask.setSubtasks(subtasks);
                    taskDao.update(superTask);
                }
            }
            taskDao.delete(id);
        } catch (NoSuchElementException exc) {
            throw exc;
        }

    }

}

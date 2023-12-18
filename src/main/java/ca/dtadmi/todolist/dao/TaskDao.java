package ca.dtadmi.todolist.dao;

import ca.dtadmi.todolist.entity.TaskEntity;
import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import ca.dtadmi.todolist.firebase.FirebaseInitialization;
import ca.dtadmi.todolist.service.MarshallService;
import com.google.api.client.util.DateTime;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class TaskDao implements Dao<TaskEntity> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Optional<TaskEntity> get(String id) {
        try {
            DocumentReference docRef = FirebaseInitialization.getTaskCollection().document(id);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = docRef.get();

            // future.get() blocks on response
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                logger.debug("Document data: {}", document.getData());
                return Optional.ofNullable(MarshallService.getTaskEntityFromFirestore(document.getData()));
            } else {
                logger.debug("No such document!");
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public Collection<TaskEntity> getAll() {
        try {
            // asynchronously retrieve all documents
            ApiFuture<QuerySnapshot> future = FirebaseInitialization.getTaskCollection().get();
            // future.get() blocks on response
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents.stream().map((QueryDocumentSnapshot document) -> {
                TaskEntity taskEntity = document.toObject(TaskEntity.class);
                taskEntity.setId(document.getId());
                return taskEntity;
            })
                .toList()
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public TaskEntity save(TaskEntity newBaseTaskEntity) {
        try {
            TaskEntity newTaskEntity = new TaskEntity(newBaseTaskEntity);
            // Add document data with auto-generated id.
            ApiFuture<DocumentReference> addedDocRef = FirebaseInitialization.getTaskCollection().add(newTaskEntity);
            logger.debug("Added document with ID: {}", addedDocRef.get().getId());

            // Update an existing document
            DocumentReference docRef = FirebaseInitialization.getTaskCollection().document(addedDocRef.get().getId());

            // (async) Update one field
            docRef.update("id", addedDocRef.get().getId());
            TaskEntity createdTask = new TaskEntity(newBaseTaskEntity);
            createdTask.setId(docRef.getId());
            return createdTask;//MarshallService.getTaskEntityFromFirestore(docRef.get().get().getData());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<TaskEntity> update(TaskEntity taskEntityUpdate) {
        try {
            // asynchronously update doc, create the document if missing
            DocumentReference docReference = FirebaseInitialization.getTaskCollection().document(taskEntityUpdate.getId());
            ApiFuture<WriteResult> writeResult =
                        docReference.set(taskEntityUpdate, SetOptions.merge());

            logger.debug("Update time : {}", writeResult.get().getUpdateTime());

            // future.get() blocks on response
            DocumentSnapshot document = docReference.get().get();
            if (document.exists()) {
                logger.debug("Document data: {}", document.getData());
                return Optional.ofNullable(MarshallService.getTaskEntityFromFirestore(document.getData()));
            } else {
                logger.debug("No such document!");
                return Optional.empty();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreExcecutionException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    public TaskEntity updateField(String id, String fieldName, Object value){
        try{
            // Update an existing document
            DocumentReference docReference = FirebaseInitialization.getTaskCollection().document(id);

            if(docReference.get().get().getData()!=null){
                // (async) Update one field
                ApiFuture<WriteResult> future = docReference.update(fieldName, value);
                WriteResult result = future.get();
                logger.debug("Write result: {}", result);

                future = docReference.update("lastModificationDate", new DateTime(new Date()).toString());
                result = future.get();
                logger.debug("Write result: {}", result);

                return get(id).orElse(null);
            }
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FirestoreExcecutionException(e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(String id) {
        try {
            // asynchronously delete a document
            ApiFuture<WriteResult> writeResult = FirebaseInitialization.getTaskCollection().document(id).delete();

            logger.debug("Delete time : {}", writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteAll() {
        List<TaskEntity> tasks = getAll().stream().toList();
        if(!tasks.isEmpty()) {
            tasks.forEach(task -> delete(task.getId()));
        }
    }

    public void deleteAllFromCollection() {
        try {
            int batchSize = 1;
            // retrieve a small batch of documents to avoid out-of-memory errors
            ApiFuture<QuerySnapshot> future = FirebaseInitialization.getTaskCollection().limit(batchSize).get();
            int deleted = 0;
            // future.get() blocks on document retrieval
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            for (QueryDocumentSnapshot document : documents) {
                document.getReference().delete();
                ++deleted;
            }
            logger.debug("Number of documents deleted: {}", deleted);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }

    }
}
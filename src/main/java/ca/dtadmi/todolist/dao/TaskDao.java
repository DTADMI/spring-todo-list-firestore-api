package ca.dtadmi.todolist.dao;

import ca.dtadmi.todolist.entity.Task;
import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import ca.dtadmi.todolist.firebase.FirebaseInitialization;
import ca.dtadmi.todolist.service.MarchallService;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
public class TaskDao implements Dao<Task> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Optional<Task> get(String id) {
        try {
            DocumentReference docRef = FirebaseInitialization.getTaskCollection().document(id);
            // asynchronously retrieve the document
            ApiFuture<DocumentSnapshot> future = docRef.get();
            // ...
            // future.get() blocks on response
            DocumentSnapshot document = future.get();
            if (document.exists()) {
                logger.debug("Document data: {}", document.getData());
                return MarchallService.unSerialize(document.getData());
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
    public Collection<Task> getAll() {
        try {
            // asynchronously retrieve all documents
            ApiFuture<QuerySnapshot> future = FirebaseInitialization.getTaskCollection().get();
// future.get() blocks on response
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            return documents.stream().map((QueryDocumentSnapshot document) -> {
                Task task = document.toObject(Task.class);
                task.setId(document.getId());
                return task;
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
    public Task save(Task newTask) {
        try {
            // Add document data with auto-generated id.
            ApiFuture<DocumentReference> addedDocRef = FirebaseInitialization.getTaskCollection().add(newTask);
            logger.debug("Added document with ID: {}", addedDocRef.get().getId());

            // Update an existing document
            DocumentReference docRef = FirebaseInitialization.getTaskCollection().document(addedDocRef.get().getId());

            // (async) Update one field
            docRef.update("id", addedDocRef.get().getId());

            Task task = new Task(newTask);
            task.setId(addedDocRef.get().getId());
            return task;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }

    @Override
    public Optional<Task> update(Task taskUpdate) {
        try {
            Task dataUpdate = new Task(taskUpdate);
            dataUpdate.setLastModificationDate(new Date().toString());
            // asynchronously update doc, create the document if missing
            DocumentReference docReference = FirebaseInitialization.getTaskCollection().document(dataUpdate.getId());
            ApiFuture<WriteResult> writeResult =
                    docReference.set(dataUpdate, SetOptions.merge());
            // ...
            logger.debug("Update time : {}", writeResult.get().getUpdateTime());

            // future.get() blocks on response
            DocumentSnapshot document = docReference.get().get();
            if (document.exists()) {
                logger.debug("Document data: {}", document.getData());
                return MarchallService.unSerialize(document.getData());
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
    public void delete(String id) {
        try {
            // asynchronously delete a document
            ApiFuture<WriteResult> writeResult = FirebaseInitialization.getTaskCollection().document(id).delete();
            // ...
            logger.debug("Update time : {}", writeResult.get().getUpdateTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            throw new FirestoreExcecutionException(e.getMessage(), e);
        }
    }
}
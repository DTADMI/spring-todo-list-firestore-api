package ca.dtadmi.todolist.firebase;

import ca.dtadmi.todolist.exceptions.FirestoreExcecutionException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FirebaseInitialization {

    private static final String COLLECTION_NAME = "Task";

    @Getter
    private static Firestore db;
    @Getter
    private static CollectionReference taskCollection;

    private final FirebaseConfigHelper helper;

    public FirebaseInitialization(FirebaseConfigHelper helper){
        this.helper = helper;
    }

    @PostConstruct
    public void initialization(){

        try {
            String servAccDataStr = helper.serviceAccountBuilder();
            InputStream strInputStream = new ByteArrayInputStream(servAccDataStr.getBytes());
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(strInputStream))
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
            taskCollection = db.collection(COLLECTION_NAME);
        } catch (IOException e) {
            throw new FirestoreExcecutionException(e);
        }
    }


}

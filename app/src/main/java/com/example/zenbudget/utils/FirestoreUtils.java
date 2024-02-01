package com.example.zenbudget.utils;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.zenbudget.classes.Event;
import com.example.zenbudget.classes.SubEvent;

public class FirestoreUtils {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference eventsCollection = db.collection("events");

    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseUser currentUser = mAuth.getCurrentUser();
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static void createEvent(String eventName, String startDate, String endDate, Double budget, FirestoreCallback<String> callback) {
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("startDate", startDate);
        eventMap.put("endDate", endDate);
        eventMap.put("eventName", eventName);
        eventMap.put("budget", budget);
        eventMap.put("userID", currentUser.getUid());
        eventsCollection
                .add(eventMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Document was added successfully, return the ID
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void getEventByID(String eventId, FirestoreCallback<Event> callback) {
        DocumentReference eventRef = eventsCollection.document(eventId);

        eventRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Document exists, parse it into an Event object
                            Event event = documentSnapshot.toObject(Event.class);
                            callback.onSuccess(event);
                        } else {
                            // Document does not exist
                            callback.onSuccess(null); // Or handle it as needed
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void getAllEvents(FirestoreCallback<List<Event>> callback) {
        eventsCollection
                .whereEqualTo("userID",currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>( ) {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        List<Event> eventList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String startDate = document.getString("startDate");
                            String endDate = document.getString("endDate");
                            String eventId = document.getId();
                            String eventTitle = document.getString("eventName");
                            Double budget = document.getDouble("budget");
                            Event event = new Event(eventId, eventTitle, startDate, endDate, budget);
                            eventList.add(event);
                        }
                        callback.onSuccess(eventList);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void getAllEventsOnSelectedDate(String selectedDate, FirestoreCallback<List<Event>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query: events with startDate <= selectedDate
        db.collection("events")
                .whereEqualTo("userID", currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        List<Event> eventList = new ArrayList<>();
                        List<Task<QuerySnapshot>> subEventTasks = new ArrayList<>();

                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String startDate = document.getString("startDate");
                            String endDate = document.getString("endDate");

                            // Check if the selectedDate is also less than or equal to endDate
                            if (selectedDate.compareTo(endDate) <= 0 && selectedDate.compareTo(startDate)>=0) {
                                String eventId = document.getId();
                                String eventTitle = document.getString("eventName");
                                Double budget = document.getDouble("budget");

                                Event event = new Event(eventId, eventTitle, startDate, endDate, budget);
                                db.collection("events").document(eventId).collection("subEvents")
                                        .whereEqualTo("subEvent_date", selectedDate)
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                                                for (QueryDocumentSnapshot subEventDocument : task.getResult()) {
                                                    String subEventId = subEventDocument.getId();
                                                    String subEventTitle = subEventDocument.getString("subEvent_title");
                                                    String subEventDate = subEventDocument.getString("subEvent_date");
                                                    String startTime = subEventDocument.getString("start_time");
                                                    String endTime = subEventDocument.getString("end_time");
                                                    Double subEventBudget = subEventDocument.getDouble("subEvent_budget");
                                                    SubEvent subEvent = new SubEvent(subEventId, subEventTitle, subEventDate, startTime, endTime, subEventBudget, eventId);
                                                    event.addSubEvent(subEvent);
                                                }
                                            } else if (task.isSuccessful()) {
                                                // Query was successful, but no documents matched the criteria.
                                                Log.d("Firestore", "No subevents found for the selected date.");
                                            } else {
                                                // Handle the error
                                                Log.e("Firestore", "Error getting subevents: ", task.getException());
                                            }
                                        });
                                eventList.add(event);
                            }
                        }

                        // Wait for all subevent tasks to complete
                        Tasks.whenAllComplete(subEventTasks)
                                .addOnCompleteListener(tasks -> callback.onSuccess(eventList));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting events: ", e);
                    callback.onError(e);
                });
    }

    public static void updateEvent(String eventId, String eventTitle, String startDate, String endDate, Double budget, FirestoreCallback<Void> callback) {
        DocumentReference eventDocRef = eventsCollection.document(eventId);

        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("startDate", startDate);
        updatedEvent.put("endDate", endDate);
        updatedEvent.put("eventName", eventTitle);
        updatedEvent.put("budget", budget);
        updatedEvent.put("userID", currentUser.getUid());

        eventDocRef
                .set(updatedEvent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Event updated successfully
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }
    public static void deleteEvent(String eventId, FirestoreCallback<Void> callback) {
        eventsCollection
                .document(eventId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Event deleted successfully
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void createSubEvent(String subEventTitle, String subEventDate, String startTime, String endTime, double budget, String eventId, FirestoreCallback<String> callback) {
        SubEvent subEvent = new SubEvent(subEventTitle,subEventDate,startTime,endTime,budget);
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .add(subEvent)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Sub-event added successfully, return the ID
                        callback.onSuccess(documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void getSubEventById(String eventId, String subEventId, FirestoreCallback<SubEvent> callback) {
        Log.d("checkData","eventid"+ eventId);
        Log.d("checkData","i m "+subEventId);
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Sub-event document exists, parse it into a SubEvent object
                            String subEventTitle = documentSnapshot.getString("subEvent_title");
                            String subEventDate = documentSnapshot.getString("subEvent_date");
                            String startTime = documentSnapshot.getString("start_time");
                            String endTime = documentSnapshot.getString("end_time");
                            double subEventBudget = documentSnapshot.getDouble("subEvent_budget");

                            SubEvent subEvent = new SubEvent(subEventId, subEventTitle, subEventDate, startTime, endTime, subEventBudget, eventId);
                            callback.onSuccess(subEvent);
                        } else {
                            // Sub-event document does not exist
                            callback.onSuccess(null); // Return null to indicate not found
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void updateSubEvent(String eventId, String subEventId, String subEventTitle, String subEventDate, String startTime, String endTime, double budget, FirestoreCallback<Void> callback) {
        SubEvent updatedData = new SubEvent(subEventTitle,subEventDate,startTime,endTime,budget);
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .set(updatedData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Sub-event updated successfully
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }

    public static void deleteSubEvent(String eventId, String subEventId, FirestoreCallback<Void> callback) {
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Sub-event deleted successfully
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle any errors
                        callback.onError(e);
                    }
                });
    }


}

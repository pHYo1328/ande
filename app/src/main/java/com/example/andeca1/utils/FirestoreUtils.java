package com.example.andeca1.utils;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.andeca1.classes.Event;
import com.example.andeca1.classes.SubEvent;

public class FirestoreUtils {
    private static final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private static final FirebaseUser currentUser = mAuth.getCurrentUser();
    public interface FirestoreCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static void createEvent(String eventName, String startDate, String endDate, Double budget, FirestoreCallback<String> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        Map<String, Object> eventMap = new HashMap<>();
        eventMap.put("startDate", startDate);
        eventMap.put("endDate", endDate);
        eventMap.put("eventName", eventName);
        eventMap.put("budget", budget);
        assert currentUser != null;
        eventMap.put("userID", currentUser.getUid());
        // Handle any errors
        eventsCollection
                .add(eventMap)
                .addOnSuccessListener(documentReference -> {
                    // Document was added successfully, return the ID
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getEventByID(String eventId, FirestoreCallback<Event> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        DocumentReference eventRef = eventsCollection.document(eventId);

        // Handle any errors
        eventRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Document exists, parse it into an Event object
                        Event event = documentSnapshot.toObject(Event.class);
                        callback.onSuccess(event);
                    } else {
                        // Document does not exist
                        callback.onSuccess(null); // Or handle it as needed
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getAllEvents(FirestoreCallback<List<Event>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        assert currentUser != null;
        // Handle any errors
        eventsCollection
                .whereEqualTo("userID",currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Event> eventList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String startDate = document.getString("startDate");
                        String endDate = document.getString("endDate");
                        String eventId = document.getId();
                        String eventTitle = document.getString("eventName");
                        Double budgetObj = document.getDouble("budget");
                        double budget = 0.0;
                        if (budgetObj != null) {
                            budget = budgetObj;
                        }
                        Event event = new Event(eventId, eventTitle, startDate, endDate, budget);
                        eventList.add(event);
                    }
                    callback.onSuccess(eventList);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getAllEventsOnSelectedDate(String selectedDate, FirestoreCallback<List<Event>> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Query: events with startDate <= selectedDate
        assert currentUser != null;
        db.collection("events")
                .whereEqualTo("userID", currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Event> eventList = new ArrayList<>();
                    List<Task<QuerySnapshot>> subEventTasks = new ArrayList<>();

                    for (QueryDocumentSnapshot document : querySnapshot) {
                        String startDate = document.getString("startDate");
                        String endDate = document.getString("endDate");

                        // Check if the selectedDate is also less than or equal to endDate
                        assert endDate != null;
                        if (selectedDate.compareTo(endDate) <= 0) {
                            assert startDate != null;
                            if (selectedDate.compareTo(startDate)>=0) {
                                String eventId = document.getId();
                                String eventTitle = document.getString("eventName");
                                Double budgetObj = document.getDouble("budget");
                                double budget = 0.0;
                                if(budgetObj!= null){
                                    budget = budgetObj;
                                }
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
                    }

                    // Wait for all subevent tasks to complete
                    Tasks.whenAllComplete(subEventTasks)
                            .addOnCompleteListener(tasks -> callback.onSuccess(eventList));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error getting events: ", e);
                    callback.onError(e);
                });
    }

    public static void updateEvent(String eventId, String eventTitle, String startDate, String endDate, Double budget, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        DocumentReference eventDocRef = eventsCollection.document(eventId);

        Map<String, Object> updatedEvent = new HashMap<>();
        updatedEvent.put("startDate", startDate);
        updatedEvent.put("endDate", endDate);
        updatedEvent.put("eventName", eventTitle);
        updatedEvent.put("budget", budget);
        assert currentUser != null;
        updatedEvent.put("userID", currentUser.getUid());

        // Handle any errors
        eventDocRef
                .set(updatedEvent)
                .addOnSuccessListener(aVoid -> {
                    // Event updated successfully
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }
    public static void deleteEvent(String eventId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        eventsCollection
                .document(eventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Event deleted successfully
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void createSubEvent(String subEventTitle, String subEventDate, String startTime, String endTime, double budget, String eventId, FirestoreCallback<String> callback) {
        SubEvent subEvent = new SubEvent(subEventTitle,subEventDate,startTime,endTime,budget);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .add(subEvent)
                .addOnSuccessListener(documentReference -> {
                    // Sub-event added successfully, return the ID
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(callback::onError);
    }

    public static void getSubEventById(String eventId, String subEventId, FirestoreCallback<SubEvent> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Sub-event document exists, parse it into a SubEvent object
                        String subEventTitle = documentSnapshot.getString("subEvent_title");
                        String subEventDate = documentSnapshot.getString("subEvent_date");
                        String startTime = documentSnapshot.getString("start_time");
                        String endTime = documentSnapshot.getString("end_time");
                        Double subEventBudgetObj = documentSnapshot.getDouble("subEvent_budget");
                        double subEventBudget = 0.0;
                        if (subEventBudgetObj != null) {
                            subEventBudget = subEventBudgetObj;
                        }
                        SubEvent subEvent = new SubEvent(subEventId, subEventTitle, subEventDate, startTime, endTime, subEventBudget, eventId);
                        callback.onSuccess(subEvent);
                    } else {
                        // Sub-event document does not exist
                        callback.onSuccess(null); // Return null to indicate not found
                    }
                })
                .addOnFailureListener(callback::onError);
    }

    public static void updateSubEvent(String eventId, String subEventId, String subEventTitle, String subEventDate, String startTime, String endTime, double budget, FirestoreCallback<Void> callback) {
        SubEvent updatedData = new SubEvent(subEventTitle,subEventDate,startTime,endTime,budget);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .set(updatedData)
                .addOnSuccessListener(aVoid -> {
                    // Sub-event updated successfully
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }

    public static void deleteSubEvent(String eventId, String subEventId, FirestoreCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsCollection = db.collection("events");
        eventsCollection
                .document(eventId)
                .collection("subEvents")
                .document(subEventId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Sub-event deleted successfully
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onError);
    }
}

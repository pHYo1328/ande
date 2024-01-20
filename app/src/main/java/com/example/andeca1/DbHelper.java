package com.example.andeca1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "myapp.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_EVENTS_TABLE = "CREATE TABLE IF NOT EXISTS events (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "event_title TEXT," +
            "start_date TEXT," +
            "end_date TEXT," +
            "budget REAL);";

    private static final String CREATE_SUBEVENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS subEvents (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "subEvent_title TEXT," +
                    "subEvent_date TEXT," +
                    "start_time TEXT," +
                    "end_time TEXT," +
                    "budget REAL," +
                    "event_id INTEGER," +
                    "FOREIGN KEY (event_id) REFERENCES events (id));";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_EVENTS_TABLE);
        db.execSQL(CREATE_SUBEVENTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS events");
        db.execSQL("DROP TABLE IF EXISTS subEvents");
        onCreate(db);
    }

    public long createEvent(String eventName, String startDate, String endDate, Double budget) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("event_title", eventName);
        values.put("start_date", startDate);
        values.put("end_date", endDate);
        values.put("budget", budget);
        long eventId = db.insert("events", null, values);
        db.close();
        return eventId;
    }

    public Event getEventByID(int eventId){
        Event event = null;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM events WHERE id = ?";
        Cursor cursor = db.rawQuery(selectQuery,new String[]{String.valueOf(eventId)});
        if(cursor.moveToFirst()){
            do{
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String eventTitle = cursor.getString(cursor.getColumnIndexOrThrow("event_title"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                double budget = cursor.getDouble(cursor.getColumnIndexOrThrow("budget"));
                event = new Event(id,eventTitle,startDate,endDate,budget);
            }while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return event;
    }

    public List<Event> getAllEvents() {
        List<Event> eventList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM events";
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String eventTitle = cursor.getString(cursor.getColumnIndexOrThrow("event_title"));
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                double budget = cursor.getDouble(cursor.getColumnIndexOrThrow("budget"));

                Event event = new Event(id, eventTitle, startDate, endDate, budget);
                eventList.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return eventList;
    }

    public List<Event> getAllEventsOnSelectedDate(String selectedDate) {
        List<Event> eventList = new ArrayList<>();
        String selectQuery = "SELECT " +
                "events.id AS event_id," +
                "events.event_title," +
                "events.start_date," +
                "events.end_date," +
                "events.budget AS eventBudget," +
                "subEvents.id AS subEvent_id," +
                "subEvents.subEvent_title," +
                "subEvents.subEvent_date," +
                "subEvents.start_time," +
                "subEvents.end_time," +
                "subEvents.budget AS subEventBudget " +
                "FROM events " +
                "LEFT JOIN subEvents ON events.id = subEvents.event_id AND subEvents.subEvent_date = ? " +
                "WHERE events.start_date <= ? AND events.end_date >= ? ";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectionArgs = {selectedDate,selectedDate,selectedDate};
        Cursor cursor = db.rawQuery(selectQuery, selectionArgs);
        if (cursor.moveToFirst()) {
            do {
                int eventId = cursor.getInt(cursor.getColumnIndexOrThrow("event_id"));

                String eventTitle = cursor.getString(cursor.getColumnIndexOrThrow("event_title"));
                Log.d("checkData","eventId"+ eventId);
                String startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));
                String endDate = cursor.getString(cursor.getColumnIndexOrThrow("end_date"));
                Double eventBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("eventBudget"));
                int subEventId = cursor.getInt(cursor.getColumnIndexOrThrow("subEvent_id"));
                String subEventTitle = cursor.getString(cursor.getColumnIndexOrThrow("subEvent_title"));
                String subEventDate = cursor.getString(cursor.getColumnIndexOrThrow("subEvent_date"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
                double subEventBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("subEventBudget"));

                Event event = new Event(eventId, eventTitle, startDate, endDate, eventBudget);
                SubEvent subEvent = new SubEvent(subEventId,subEventTitle, subEventDate, startTime, endTime, subEventBudget, eventId);

                // Check if the event is already in the list
                boolean eventExists = false;
                for (Event existingEvent : eventList) {

                    if (existingEvent.getId() != null && existingEvent.getId() == eventId) {
                        existingEvent.addSubEvent(subEvent);
                        eventExists = true;
                        break;
                    }
                }

                // If the event is not in the list, add both the event and subevent
                if (!eventExists) {

                    event.addSubEvent(subEvent);
                    eventList.add(event);
                }
            } while (cursor.moveToNext());
        }
        Log.d("checkDate","db "+selectedDate+eventList.toString());
        cursor.close();

        return eventList;
    }

    public int updateEvent(int eventId, String eventTitle, String startDate, String endDate,Double budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("event_title", eventTitle);
        values.put("start_date", startDate);
        values.put("end_date",endDate);
        values.put("budget",budget);
        return db.update("events", values, "id=?", new String[]{String.valueOf(eventId)});
    }

    public void deleteEvent(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("events", "id=?", new String[]{String.valueOf(eventId)});
        db.close();
    }

    public long createSubEvent(String subEventTitle, String subEventDate, String startTime, String endTime, double budget, int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("subEvent_title", subEventTitle);
        values.put("subEvent_date", subEventDate);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("budget", budget);
        values.put("event_id", eventId);
        long subEventId = db.insert("subEvents", null, values);
        db.close();
        return subEventId;
    }

    public SubEvent getSubEventById(int subEventId){

        SubEvent subEvent = null;
        String selectQuery = "SELECT * FROM subEvents WHERE id = ?";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(subEventId)});
        if (cursor.moveToFirst()) {
            do {
                String subEventTitle = cursor.getString(cursor.getColumnIndexOrThrow("subEvent_title"));
                String subEventDate = cursor.getString(cursor.getColumnIndexOrThrow("subEvent_date"));
                String startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                String endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
                double subEventBudget = cursor.getDouble(cursor.getColumnIndexOrThrow("budget"));
                int eventId = cursor.getInt(cursor.getColumnIndexOrThrow("event_id"));
                subEvent = new SubEvent(subEventId, subEventTitle, subEventDate, startTime, endTime, subEventBudget, eventId);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return subEvent;
    }

    public int updateSubEvent(int subEventId, String subEventTitle, String subEventDate, String startTime, String endTime, double budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("subEvent_title", subEventTitle);
        values.put("subEvent_date", subEventDate);
        values.put("start_time", startTime);
        values.put("end_time", endTime);
        values.put("budget", budget);
        return db.update("subEvents", values, "id=?", new String[]{String.valueOf(subEventId)});
    }

    public void deleteSubEvent(int subEventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("subEvents", "id=?", new String[]{String.valueOf(subEventId)});
        db.close();
    }

}

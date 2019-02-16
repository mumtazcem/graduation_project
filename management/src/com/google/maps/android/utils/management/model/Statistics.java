package com.google.maps.android.utils.management.model;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class Statistics {
    public FileOutputStream queueComparisonStaticOpen, queueComparisonDynamicOpen,
    distTravStaticOpen, distTravDynamicOpen;

    public File queueSizeStaticFile, queueSizeDynamicFile, distTravStaticFile, distTravDynamicFile;

    public OutputStreamWriter queueComparisonStatic, queueComparisonDynamic,
            distTravStatic, distTravDynamic;
    public Statistics(Context context) {
        try {
            /*queueSizeStaticFile = new File(context.getFilesDir(), "queueSizeStatic.txt");
            queueSizeDynamicFile = new File(context.getFilesDir(), "queueSizeDynamic.txt");
            distTravStaticFile = new File(context.getFilesDir(), "distTravStatic.txt");
            distTravDynamicFile = new File(context.getFilesDir(), "distTravDynamic.txt");

            Log.e("File", "Files are created");
            queueComparisonStaticOpen = context.openFileOutput("queueSizeStatic.txt", Context.MODE_PRIVATE);
            queueComparisonDynamicOpen = context.openFileOutput("queueSizeDynamic.txt", Context.MODE_PRIVATE);
            distTravStaticOpen = context.openFileOutput("distTravStatic.txt", Context.MODE_PRIVATE);
            distTravDynamicOpen = context.openFileOutput("distTravDynamic.txt", Context.MODE_PRIVATE);

            queueComparisonStatic = new OutputStreamWriter(queueComparisonStaticOpen);
            queueComparisonDynamic = new OutputStreamWriter(queueComparisonDynamicOpen);
            distTravStatic = new OutputStreamWriter(distTravStaticOpen);
            distTravDynamic = new OutputStreamWriter(distTravDynamicOpen);*/

            queueComparisonStatic = new OutputStreamWriter(context.openFileOutput("queueComparisonStatic.txt", Context.MODE_PRIVATE));
            queueComparisonDynamic = new OutputStreamWriter(context.openFileOutput("queueComparisonDynamic.txt", Context.MODE_PRIVATE));
            distTravStatic = new OutputStreamWriter(context.openFileOutput("distTravStatic.txt", Context.MODE_PRIVATE));
            distTravDynamic = new OutputStreamWriter(context.openFileOutput("distTravDynamic.txt", Context.MODE_PRIVATE));
        } catch (IOException e) {
            Log.e("Exception", "File write failed: "+ e.toString());
        }
    }

    /* if it is staticSWM then bool value is true
     *  and statistics for static file will be written
     *  else statics for dynamic file will be written.
     *
     *  this function is to compare dynamic and static
     *  trigger queue sizes for the period.
     *  which one has lower queue size over
     *  time etc.*/
    public void writeToFileQueueSizeStatic(Double clockData, int size, Context context){
        try {
            String to_write = "";
            to_write = String.valueOf(clockData) + " " + String.valueOf(size);
            queueComparisonStatic.write(to_write);
            Log.e("File", "writeToFileQueueSizeStatic worked: "+ to_write);
        } catch (IOException e) {
            Log.e("File", "File write failed: "+ e.toString());
        }

    }

    public void writeToFileQueueSizeDynamic(Double clockData, int size, Context context){
        try {
            String to_write = "";
            to_write = String.valueOf(clockData) + " " + String.valueOf(size);
            queueComparisonDynamic.write(to_write);
            Log.e("File", "writeToFileQueueSizeDynamic worked: "+ to_write);
        } catch (IOException e) {
            Log.e("File", "File write failed: "+ e.toString());
        }

    }

    /* statistics of distance travelled over time*/
    public void writeToFileDistTravStatic(Double clockData, int distance, Context context){
        try {
            String to_write = "";
            to_write = String.valueOf(clockData) + " " + String.valueOf(distance);
            distTravStatic.write(to_write);
            Log.e("File", "writeToFileDistTravStatic worked: "+ to_write);

        } catch (IOException e) {
            Log.e("File", "File write failed: "+ e.toString());
        }

    }

    public void writeToFileDistTravDynamic(Double clockData, int distance, Context context){
        try {
            String to_write = "";
            to_write = String.valueOf(clockData) + " " + String.valueOf(distance);
            distTravDynamic.write(to_write);
            Log.e("File", "writeToFileDistTravDynamic worked: "+ to_write);

        } catch (IOException e) {
            Log.e("File", "File write failed: "+ e.toString());
        }

    }

    public void closeStatFiles(Context context){
        try {
            queueComparisonDynamic.close();
            queueComparisonStatic.close();;
            distTravDynamic.close();
            distTravStatic.close();
        } catch (IOException e) {
            Log.e("File", "File write failed: "+ e.toString());
        }
    }

}

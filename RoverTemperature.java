// Simulation Idea: Create a shared clock object that broadcasts
// time changes and calls to take temperature readings to all
// sensors (threads)

import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Collections;
import java.util.Arrays;

public class RoverTemperature
{
  public static void main(String[] args)
  {
    int numThreads = 8;
    int hoursToRecord = 1;

    ThreadRunner rover = new ThreadRunner(numThreads, hoursToRecord);
    ArrayList<Thread> sensors = new ArrayList<>();

    for (int i = 0; i < numThreads; i++)
    {
      sensors.add(new Thread(rover, String.valueOf(i)));
    }

    for (Thread sensor : sensors)
      sensor.start();
  }
}

class ThreadRunner implements Runnable
{
  int maxTime;
  int numSensors;
  ConcurrentLinkedQueue<Reading> log;

  // Reading is a data structure to hold information about a temperature reading,
  // the sensor that collected it, and when it was collected.
  class Reading
  {
    int sensorID;
    int time;
    int temperature;

    public Reading(int sensorID, int time, int temperature)
    {
      this.sensorID = sensorID;
      this.time = time;
      this.temperature = temperature;
    }
  }

  // Maxtime is the maximum number of hours to collect data for
  public ThreadRunner(int numSensors, int maxTime)
  {
    this.maxTime = maxTime;
    this.numSensors = numSensors;
    log = new ConcurrentLinkedQueue<Reading>();
  }

  public void run()
  {
    // We elect one thread to be the "leader". The leader handles time, alerts
    // other sensors to take a reading, and compiles the report at the end of
    // an hour
    boolean leader = Thread.currentThread().getName().equals("0") ? true : false;
    int time = 0;
    int hoursElapsed = 0;

    int temp;

    // Sensors continuously run; one minute is one execution of this while loop.
    // Sensors read the temperature once every minute.
    while(true)
    {

      temp = ReadTemperature(time);

      Reading reading = new Reading(Integer.parseInt(Thread.currentThread().getName()), time, temp);

      // All threads (including the leader) take a reading, and add it to the
      // log. At the end of each hour, the leader compiles these into report.
      log.add(reading);
      time++;
      if(time >= 60)
      {
        if(leader)
          CompileReport(hoursElapsed);
        time = 0;
        hoursElapsed++;
      }
      if (hoursElapsed >= maxTime)
        return;
    }

  }

  public void CompileReport(int currentHour)
  {
    boolean debug = false;
    // Note: size is NOT a constant time method, as it may be modified

    System.out.println("Thread " + Thread.currentThread().getName() + " compiling a report...");
    int logSize = log.size();
    System.out.println("There are " + logSize + " readings recorded.");
    Reading[] readings = log.toArray(new Reading[logSize]);

    // We only care about unique temperatures recorded
    // A true value means that temperature was recorded
    // We also check that the time on the sensor is within the current hour,
    // necesary as the threads may continue to collect data during report compilation
    boolean[] temperatures = new boolean[171];
    for (int i = 0; i < logSize; i++)
    {
      if (readings[i].time / 60 == currentHour) // This reading was taken after this hour, so discard
        continue;
      temperatures[readings[i].temperature + 100] = true;
    }

    // Collections.sort(temperatures);
    // Arrays.sort(temperatures);
    System.out.println("5 Lowest Recorded Temperatures:");
    int count = 0, temp = 0;
    while (count < 5)
    {
      if (temperatures[temp])
      {
        System.out.println((count + 1) + ": " + (temp - 100) + "F");
        count++;
      }
      temp++;
    }
    System.out.println("5 Highest Recorded Temperatures:");
    count = 0;
    temp = 170;
    while (count < 5)
    {
      if (temperatures[temp])
      {
        System.out.println((count + 1) + ": " + (temp - 100) + "F");
        count++;
      }
      temp--;
    }

    // Now we must find what temperatures each sensor recorded at each time
    // We know that for a 1 hour report, we should have 480 readings
    // The first dimension holds the time, while the second dimension represents the sensor
    int[][] temperatureTimes = new int[60][numSensors];
    for (int i = 0; i < readings.length; i++)
    {
      if (readings[i].time / 60 == currentHour) // If the reading occured during this hour's report=
        temperatureTimes[readings[i].time][readings[i].sensorID] = readings[i].temperature;
    }

    int maxDifferenceTime = 0;
    int maxDifference = 0;
    System.out.println("Time period of most temperature difference:");

    if (debug) // Debugging
    {
      for (int i = 0; i < 60; i++)
      {
        System.out.println("Time " + i + ": ");
        for (int j = 0; j < numSensors; j++)
        {
          System.out.println("   Sensor " + j + ": " + temperatureTimes[i][j]);
        }
      }
    }
    // We compare sensors against their own readings to find the greatest difference
    // at each possible time
    int sensor = -1;
    for (int i = 0; i < 50; i++)
    {
      for (int j = 0; j < numSensors; j++)
      {
        // If the difference in temperature in 10 minutes on this sensor is
        // larger than the largest difference prevously recorded
        if (Math.abs(temperatureTimes[i + 10][j] - temperatureTimes[i][j]) >
            maxDifference)
          {
            maxDifference = Math.abs(temperatureTimes[i + 10][j] - temperatureTimes[i][j]);
            maxDifferenceTime = i;
            sensor = j;
          }
      }
    }

    System.out.println("Ten minute period from " + currentHour + ":"  + maxDifferenceTime
                        + " - " + currentHour + ":" + (maxDifferenceTime + 10)
                        + ", with a temperature difference of " +
                        Math.abs(temperatureTimes[maxDifferenceTime + 10][sensor] - temperatureTimes[maxDifferenceTime][sensor]) + "F!");
    System.out.println("Data collected on sensor " + sensor);
  }



  // Generates a random integer in [-100,70] to simulate a temperature reading
  // We use the time as the seed to represent sensors taking a reading at the same time.
  public int ReadTemperature(int seed)
  {
    Random rand = new Random();
    return rand.nextInt(171) - 100;
  }
}

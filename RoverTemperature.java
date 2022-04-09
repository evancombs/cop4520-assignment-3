// Simulation Idea: Create a shared clock object that broadcasts
// time changes and calls to take temperature readings to all
// sensors (threads)

import java.util.Random;
import java.util.ArrayList;

public class RoverTemperature
{
  public static void main(String[] args)
  {
    int numThreads = 8;

    ThreadRunner rover = new ThreadRunner();
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

  public ThreadRunner()
  {

  }

  public void run()
  {
    // We elect one thread to be the "leader". The leader handles time, alerts
    // other sensors to take a reading, and compiles the report at the end of
    // an hour
    boolean leader = Thread.currentThread().getName().equals("0") ? true : false;
    int leaderTime = 0;

    int temp;
    // Sensors continuously run; one minute is one execution of this while loop.
    // Sensors read the temperature once every minute.
    while(true)
    {
      temp = ReadTemperature();

      Reading reading = new Reading(parseInt(Thread.currentThread.getName()),)

      // All threads (including the leader) take a reading, and add it to the
      // log. After an hour, the leader

      if(leader)
      {
        leaderTime++;
        if (leaderTime >= 60)
        {
          CompileReport();
          leaderTime = 0;
        }
      }
    }

  }

  public void CompileReport()
  {

  }


  // Generates a random integer in [-100,70]
  public int ReadTemperature()
  {
    Random rand = new Random();
    return rand.nextInt(170) - 100;
  }
}

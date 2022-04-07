import java.util.Random;

public class RoverTemperature
{
  public int ReadTemperature()
  {
    Random rand = new Random();
    return rand.nextInt(170) - 100;
  }

}

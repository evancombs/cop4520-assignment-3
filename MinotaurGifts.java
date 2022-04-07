import java.util.ArrayList;

public class MinotaurGifts
{
  public static void main(String[] args)
  {
    int numServants = 4;

    ThreadRunner threadRunner = new ThreadRunner();
    ArrayList<Thread> servants = new ArrayList<>();

    for (int i = 0; i < numServants; i++)
      servants.add(new Thread(threadRunner));

    for(Thread servant : servants)
      servant.start();
  }


}

class ThreadRunner implements Runnable
{
  
  public void run()
  {

  }
}

class GiftList<T>
{
  class Node
  {
    T gift;
    int key;
    Node next;
  }

}

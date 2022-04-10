import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class MinotaurGifts
{
  public static void main(String[] args)
  {
    int numServants = 4;
    int numGifts = 500000;

    ThreadRunner threadRunner = new ThreadRunner(numServants, numGifts);
    ArrayList<Thread> servants = new ArrayList<>();

    for (int i = 0; i < numServants; i++)
      servants.add(new Thread(threadRunner));

    System.out.println("Preparing to sort " + numGifts + " gifts with " + numServants
                        + " servants...");
    long start = System.currentTimeMillis();
    for(Thread servant : servants)
      servant.start();

    try
    {
      for(Thread servant : servants)
        servant.join();
    }
    catch(Exception e)
    {
      System.out.println("Failed to join threads!");
      System.out.println(e);
    }

    System.out.println("All thank you cards have been written!");
    System.out.println("Total runtime: " + (System.currentTimeMillis() - start) + "ms");
  }


}

class ThreadRunner implements Runnable
{
  GiftList giftChain;

  // We use a ConcurrentLinkedQueue to keep track of all the unsorted gifts
  ConcurrentLinkedQueue<String> unsortedGifts;

  AtomicInteger giftsRemaining;

  int numServants;

  public ThreadRunner(int servants, int numGifts)
  {
    this.numServants = servants;
    giftsRemaining = new AtomicInteger(numGifts);

    // We make use of the ConcurrentLinkedQueue
    unsortedGifts = GenerateRandomGifts(numGifts);

    // For this simulation, assume that the Minotaur's gifts all gifted him
    // their favorite words! Some of his guests are strange, and have favorite
    // words like 'aaaaaaa' and 'djfnjknsadfg'

    // System.out.println(unsortedGifts.poll());
    // System.out.println(unsortedGifts.poll());
    // System.out.println(unsortedGifts.poll());
    giftChain = new GiftList();
  }

  public void run()
  {
    // While there are gifts in the pile, we add them to the chain
    while (giftsRemaining.decrementAndGet() > 0)
    {
      // Servants are asked to alternate adding and removing gifts

      // Simulate picking a unsorted gift by generating a random gift
      giftChain.add(unsortedGifts.poll());

      // Pop simply calls remove() on the first gift in the chain. Unless
      // the servants get instructions to remove a gift, they just remove
      // the first one, to ensure that all gifts are removed.
      giftChain.pop();
    }
  }

  // Returns a randomly ordered list of the first
  private ConcurrentLinkedQueue<String> GenerateRandomGifts(int size)
  {
    ConcurrentLinkedQueue<String> bag = new ConcurrentLinkedQueue<>();
    // There are 26^5 (11881376) unique 5-character strings over [a-z] --
    // far more than enough to ensure that each gift is unique.
    // String str;
    int count = 0;
    for (int d0 = 0; d0 < 26; d0++)
      for (int d1 = 0; d1 < 26; d1++)
        for (int d2 = 0; d2 < 26; d2++)
          for (int d3 = 0; d3 < 26; d3++)
            for (int d4 = 0; d4 < 26; d4++)
            {
              count++;
              char[] str = {(char)('a' + d4),(char)('a' + d3),(char)('a' + d2),(char)('a' + d1),(char)('a' + d0)};
              bag.add(new String(str));
              if (count >= size)
                return bag;
            }

    return bag;
  }
}

// GiftList is based on a modified LockFreeList from the Art of MultiProcessor
// Programming
class GiftList
{
  Node head;

  class Node
  {
    String gift;
    int key;
    AtomicMarkableReference<Node> next;

    public Node(String gift)
    {
      this.gift = new String(gift);
      key = gift.hashCode();
    }
  }

  class Window
  {
    Node pred, curr;
    public Window(Node pred, Node curr)
    {
      this.pred = pred;
      this.curr = curr;
    }
  }

  public Window find(Node head, int key)
  {
    Node pred = null, curr = null, succ = null;
    boolean[] marked = {false};
    boolean snip;

    retry: while(true)
    {
      pred = head;
      // System.out.println(pred.gift);
      // System.out.println(pred.next.getReference().gift);
      curr = pred.next.getReference();
      while(true)
      {
        if (curr.next == null)
          ;
        else
          succ = curr.next.get(marked);
        while(marked[0])
        {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip)
            continue retry;
          curr = succ;
          if (curr.next == null)
            ;
          else
            succ = curr.next.get(marked);
        }
        if (curr.key >= key)
          return new Window(pred, curr);
        pred = curr;
        curr = succ;
      }
    }
  }

  public GiftList()
  {
    head = new Node("HEAD");
    head.key = Integer.MIN_VALUE;

    Node tail = new Node("TAIL");
    tail.key = Integer.MAX_VALUE;

    head.next = new AtomicMarkableReference<Node>(tail, false);
  }
  public boolean add(String gift)
  {
    int key = gift.hashCode();
    while (true)
    {
      Window window = find(head, key);
      Node pred = window.pred, curr = window.curr;
      if (curr.key == key)
      {
        return false;
      }
      else
        {
          Node node = new Node(gift);
          node.next = new AtomicMarkableReference<Node>(curr, false);
          if (pred.next.compareAndSet(curr, node, false, false))
            {
              // System.out.println("Added " + gift);
              return true;
            }
        }
    }
  }

  public boolean remove(String gift)
  {
    int key = gift.hashCode();
    boolean snip;
    while (true)
    {
      Window window = find(head, key);
      Node pred = window.pred, curr = window.curr;
      if (curr.key != key)
      {
        return false;
      }
      else
      {
        Node succ = curr.next.getReference();
        snip = curr.next.compareAndSet(succ, succ, false, true);
        if (!snip)
          continue;
        pred.next.compareAndSet(curr, succ, false, false);
        // System.out.println("Removed " + gift);
        return true;
      }
    }
  }

  // A newly introduced list function; removes the first element, regardless of
  // its key; used to ensure that all
  public boolean pop()
  {
    String top = head.next.getReference().gift;
    return remove(top);
  }

  public boolean contains(String gift)
  {
    boolean[] marked = {false};
    int key = gift.hashCode();
    Node curr = head;
    while (curr.key < key)
    {
      curr = curr.next.getReference();
      Node succ = curr.next.get(marked);
    }
    return (curr.key == key && !marked[0]);
  }
}

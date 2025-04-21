/* カードをシャッフルして最初の２枚のカードを決定 */

public class CardClass{

  int card1;
  int card2;

  public CardClass(){
    card1=0;
    card2=0;
  }
  
  
  public void shuffle()
    {
      int m,n;

      m = (int)(Math.random()*52);
      n = (int)(Math.random()*51);
      card1 = m / 4 + 2;
      if ( n >= m ) card2 = (n + 1) / 4 +2;
      else card2 = n / 4 + 2;

  }

}

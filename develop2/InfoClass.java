public class InfoClass{

	public int opponent_money;	//相手の所持金
	public int my_money;		//自分の所持金
	public int opponent_card;	//相手のカード
	public int my_card;		//自分のカード
	public int opponent_bid;	//相手のビッド額
	public int my_bid;		//自分のビッド額
	public char opponent_decision;	//相手のコールorドロップの判断
	public char my_decision;	//自分のコールorドロップの判断

//コンストラクタ
	public InfoClass(){

		opponent_money = 30;
		my_money = 30;
		opponent_card = 0;
		my_card = 0;
		opponent_bid = 0;
		my_bid = 0;
		opponent_decision = 'n';
		my_decision = 'n';

	}

}

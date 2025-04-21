/* 知識クラス */

//import java.*;
import java.lang.Math;

public class KnowledgeClass {

	InfoClass current = new InfoClass(); // ゲーム情報
	InfoClass previous = new InfoClass(); // 直前のゲーム情報

	// 過去10回分のゲーム履歴情報
	// n回前のゲーム情報をhistory[n-1]に記憶
	InfoClass[] history = new InfoClass[10];

	String decision; // コールorドロップの宣言用
	String bid; // ビッド額宣言用

	KnowledgeClass() {
		for (int i = 0; i < history.length; i++) {
			history[i] = new InfoClass();
		}
	}

	// ビッド数の決定
	public String bid() {

		// ビッドする前にゲーム履歴情報を更新する
		HistoryUpdate();

		int b = 0; // ビッド額
		bid = ""; // 初期化

		b = Math.min(current.my_money, Math.min((current.my_money / 5) + 1,
				(8 / (current.opponent_card + 1)) + 1));

		// ビッド額のチェック(自分の残金、相手の残金を超えた額は宣言できない)
		if (b > current.opponent_money)
			b = current.opponent_money;
		if (b > current.my_money)
			b = current.my_money;

		// 返り値は String クラスで
		bid = "" + b;

		return bid;

	}

	// コール or ドロップの決定ルール
	public String decision() {

		decision = "n"; // 初期化

		// 履歴 history から自分のカード mycard を予測する
		// 履歴から予測できない場合は初期値9としておく。
		int mycard =9;

		// 現在の相手のビッド額と同じビッド額を、過去に相手が賭けていれば、
		// 自分のカードは、そのときのカードと同じであると予測する。
		for(int i=0; i<history.length;i++){

		    // 相手が今回と同じビッド額をかけたゲームが複数あっても
		    // そのゲームの中から最新の情報だけを用いる。
		    if(current.opponent_bid == history[i].opponent_bid){
			mycard = history[i].my_card;
			break;                            
		    }
		}

                //予測した mycard よりも相手のカードが強いとドロップ
		if (current.opponent_card > mycard)
			decision = "d";
		else
			decision = "c";

		// 返り値は String クラスで
		return decision;
	}


	// historyに直前のゲーム情報 previous を格納する
	private void HistoryUpdate() {

		// 履歴内の最古のゲーム情報を破棄する。
		for (int i = history.length - 2; i >= 0; i--) {
			history[i + 1] = CopyInfo( history[i] );
		}

		// 直前のゲーム情報を履歴に残す
		history[0] = CopyInfo( previous );

	}

	
        // InfoClassのインスタンスをコピーする
	private InfoClass CopyInfo(InfoClass Info) {
		
		InfoClass tmpInfo = new InfoClass();
		tmpInfo.my_bid = Info.my_bid;
		tmpInfo.my_card = Info.my_card;
		tmpInfo.my_decision = Info.my_decision;
		tmpInfo.my_money = Info.my_money;
		tmpInfo.opponent_bid = Info.opponent_bid;
		tmpInfo.opponent_card = Info.opponent_card;
		tmpInfo.opponent_decision  = Info.opponent_decision;
		tmpInfo.opponent_money = Info.opponent_money;
		return tmpInfo;
	    
	}
}


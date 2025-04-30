/* 知識クラス */

//import java.*;
import java.lang.Math;

public class KnowledgeClass {

	InfoClass current = new InfoClass(); // ゲーム情報
	InfoClass previous = new InfoClass(); // 直前のゲーム情報

	// 過去1000回分のゲーム履歴情報
	// n回前のゲーム情報をhistory[n-1]に記憶
	InfoClass[] history = new InfoClass[1000];

	// 履歴を二次元配列で保存
	int[][] historyMatrix = new int[5][14]; // 5行（ビッド数1～5）、14列（カード値1～14）

	String decision; // コールorドロップの宣言用
	String bid; // ビッド額宣言用

	KnowledgeClass() {
		for (int i = 0; i < history.length; i++) {
			history[i] = new InfoClass();
		}
	}

	// ビッド数の決定
	public String bid() {
		int b;
		bid = "";
		HistoryUpdate(); // ビッドする前にゲーム履歴情報を更新する

		// ビッドの値をデフォルト関数を使うかa_mode関数を使うかランダムで決定する
		if (Math.random() < 0.8) {
			b = bid_default(); // デフォルト関数を使う
		} else {
			b = bid_mode_a(); // a_mode関数を使う
		}

		// ビッド額のチェック(自分の残金、相手の残金を超えた額は宣言できない)
		if (b > current.opponent_money)
			b = current.opponent_money;
		if (b > current.my_money)
			b = current.my_money;

		// ビッド額が1から5の間に
		if (b < 1) {
			b = 1;
		} else if (b > 5) {
			b = 5;
		}
		System.out.println(current.opponent_bid + "" + predictOpponentCard(current.opponent_bid));
		return "" + b; // 戻り値を明示的に指定
	}

	public int bid_default() {
		int b = 0; // ビッド額
		// 初期化

		b = Math.min(current.my_money, Math.min((current.my_money / 5) + 1,
				(8 / (current.opponent_card + 1)) + 1));
		return b;
	}

	public int bid_mode_a() {
		int b = 0; // ビッド額
		if (11 <= current.opponent_card && current.opponent_card <= 14) {
			b = 1;
		} else if (7 <= current.opponent_card && current.opponent_card <= 10) {
			b = 2;
		} else if (5 <= current.opponent_card && current.opponent_card <= 6) {
			b = 3;
		} else if (3 <= current.opponent_card && current.opponent_card <= 4) {
			b = 4;
		} else {
			b = 5;
		}
		return b;
	}

	// コール or ドロップの決定ルール
	public String decision() {
		decision = "n";
		// if (Math.random() < 0.5) {
		// 	decision = decision_default(); // デフォルト関数を使う
		// } else {
		// 	decision = decision_mode_a(); // a_mode関数を使う
		// }
		decision = decision_default();
		return decision;
	}

	public String decision_default() {
		decision = "n"; // 初期化

		// 履歴 history から自分のカード mycard を予測する
		float mycard = predictOpponentCard(current.opponent_bid);
		System.out.println(current.opponent_bid + "" + predictOpponentCard(current.opponent_bid));
		// 予測した mycard よりも相手のカードが強いとドロップ
		if (current.opponent_card > mycard) {
			decision = "d";
		} else {
			decision = "c";
		}

		// 返り値は String クラスで
		return decision;
	}

	public String decision_mode_a() {
		decision = "n"; // 初期化
		if (current.opponent_card == 2 || current.opponent_card == 3) {
			decision = "c";
		} else if (current.opponent_card == 4 || current.opponent_card == 5 || current.opponent_card == 6) {
			if (current.opponent_card - current.my_card >= 4) {
				decision = "d";
			} else {
				decision = "c";
			}
		} else if (current.opponent_card == 7 || current.opponent_card == 8 || current.opponent_card == 9) {
			if (current.opponent_card - current.my_card >= 3) {
				decision = "d";
			} else {
				decision = "c";
			}
		} else if (current.opponent_card == 10 || current.opponent_card == 11 || current.opponent_card == 12) {
			if (current.opponent_card - current.my_card >= 2) {
				decision = "d";
			} else {
				decision = "c";
			}
		} else if (current.opponent_card == 13 || current.opponent_card == 14) {
			decision = "d";
		}
		return decision;
	}

	// historyに直前のゲーム情報 previous を格納する
	private void HistoryUpdate() {

		// 履歴内の最古のゲーム情報を破棄する。
		for (int i = history.length - 2; i >= 0; i--) {
			history[i + 1] = CopyInfo(history[i]);
		}

		// 直前のゲーム情報を履歴に残す
		history[0] = CopyInfo(previous);

	}

	// 履歴を更新するメソッド
	private void updateHistoryMatrix(int opponentBid, int myCard) {
		if (opponentBid >= 1 && opponentBid <= 5 && myCard >= 1 && myCard <= 14) {
			historyMatrix[opponentBid - 1][myCard - 1]++;
		}
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
		tmpInfo.opponent_decision = Info.opponent_decision;
		tmpInfo.opponent_money = Info.opponent_money;
		return tmpInfo;

	}

	// ブラフ判定関数
	public boolean isBluffing(int opponent_card, int opponent_bid) {
		boolean isLowCard = opponent_card < 5; // カードが低いか
		boolean isHighBid = opponent_bid > opponent_card * 2; // ビッド額がカードの2倍を超えているか

		return isLowCard && isHighBid; // 条件を満たせばブラフと判定
	}

	// 重み付きの相手カード予測関数
	public float predictOpponentCard(int opponent_bid) {
		int weightedSum = 0;
		int weightTotal = 0;

		// historyMatrixを基に重み付けでカードを予測
		if (opponent_bid >= 1 && opponent_bid <= 5) {
			for (int card = 1; card <= 14; card++) {
				int weight = historyMatrix[opponent_bid - 1][card - 1]; // 該当ビッドとカードの履歴回数を重みとする
				weightedSum += card * weight; // カード値に重みを掛けて加算
				weightTotal += weight; // 重みを加算
			}
		}

		// 平均値を計算して返す
		if (weightTotal > 0) {
			System.out.println( Math.round((double) weightedSum / weightTotal));
			return Math.round((double) weightedSum / weightTotal); // 平均値を計算して四捨五入
		} else {
			return 9; // 予測がうまくできない場合は9を返す
		}
	}
}


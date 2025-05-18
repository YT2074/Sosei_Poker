/* サーバプログラム */

// ライブラリの利用
import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
  
public static void main(String args[]) {
			
	int portnum              = 0;	//ポートNo.
	String pnumber           = new String();	//ポート番号入力用
	int game_num = 1; 		//ゲーム数カウント用変数
	boolean game = true;		//ゲームの終了判定用変数

	String p1_bid;	//ビッド額受信用
	String p2_bid;	
	String decision;	//コールorドロップ宣言受信用

//シャッフル用クラス shuf 初期化
	CardClass shuf = new CardClass();

//カード情報管理関数 info 初期化(my_はプレイヤ1用、opponent_はプレイヤ2用)
	InfoClass info = new InfoClass();	

//ポート番号入力
	try {
            System.err.println("ポート番号を入力してください (Enter で自動割り当て)");
            InputStreamReader isr = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(isr);
            pnumber = br.readLine();
            if (pnumber == null || pnumber.equals(""))
                pnumber = "0";
            portnum = Integer.parseInt(pnumber);
	}
	catch(Exception e) {
            System.err.println("入力エラーです．");
            System.exit(1);
	}
  
	try{
	//portnum におけるサーバソケット作成
		ServerSocket ss = new ServerSocket(portnum);

		System.out.println("参加者受付");
                System.out.println("サーバ名: " + java.net.InetAddress.getLocalHost().getHostName());
                System.out.println("ポート番号: "+ ss.getLocalPort());
                

	//クライアント１からの要求受付け
		Socket s1 = ss.accept();

	//ストリーム準備＆登録受け付け
		InputStream is1 = s1.getInputStream();
		DataInputStream dis1 = new DataInputStream(is1);
		System.out.println("プレイヤ１が参加します");

		OutputStream os1 = s1.getOutputStream();
		DataOutputStream dos1 = new DataOutputStream(os1);
		dos1.writeUTF("登録完了&ゲーム開始");
			
	//クライアント２からの要求受付け
		Socket s2 = ss.accept();

	//ストリーム準備＆登録受け付け
		InputStream is2 = s2.getInputStream();
		DataInputStream dis2 = new DataInputStream(is2);
		System.out.println("プレイヤ２が参加します");

		OutputStream os2 = s2.getOutputStream();
		DataOutputStream dos2 = new DataOutputStream(os2);
		dos2.writeUTF("登録完了&ゲーム開始");

		System.out.println("プレイヤ１とプレイヤ２の登録を受け付けました");

		System.out.println("ゲームを開始します");

		System.out.println("プレイヤ１の所持金 " + info.my_money 
+ "\n" + "プレイヤ２の所持金 " + info.opponent_money + "\n");


	//ゲームの開始
		while (game) {

	//初期化
		p1_bid = "";
		p2_bid = "";	
		decision = "n";

	//ゲーム数表示
		System.out.println(game_num + " ゲーム目を開始します");

	//シャッフル&ディール	
		shuf.shuffle();
		info.my_card = shuf.card1;
		info.opponent_card = shuf.card2;

		System.out.println("プレイヤ１のカード：" + card(info.my_card));
		System.out.println("プレイヤ２のカード：" + card(info.opponent_card));

	//それぞれの相手のカード情報送信

		dos1.writeInt(info.opponent_card);
		dos2.writeInt(info.my_card);
			
	//それぞれのビッド額受信

		p1_bid = dis1.readUTF();
		p2_bid = dis2.readUTF();

		System.out.println("プレイヤ１のビッド：" + p1_bid);
		System.out.println("プレイヤ２のビッド：" + p2_bid);

	//ビッドエラーチェック
		if (p1_bid.equals("1") || p1_bid.equals("2") || p1_bid.equals("3") || p1_bid.equals("4") || p1_bid.equals("5"))
			info.my_bid = Integer.parseInt(p1_bid);
		else 
			info.my_bid = 0;

		if (p2_bid.equals("1") || p2_bid.equals("2") || p2_bid.equals("3") || p2_bid.equals("4") || p2_bid.equals("5"))
			info.opponent_bid = Integer.parseInt(p2_bid);
		else
			info.opponent_bid = 0;

	//プレイヤ１、２共にエラーの場合
		if((info.my_bid == 0 || info.my_bid > info.my_money || info.my_bid > info.opponent_money) && (info.opponent_bid == 0 || info.opponent_bid > info.my_money || info.opponent_bid > info.opponent_money))
		{
			System.err.println("プレイヤ１とプレイヤ２のビッドはエラーです");
			info.my_money = 30;
			info.opponent_money = 30;
			dos1.writeInt(8);
			dos2.writeInt(8);
		
			break;
		}

	//プレイヤ１のビッドエラー
		if(info.my_bid == 0 || info.my_bid > info.my_money || info.my_bid > info.opponent_money)
		{
			System.err.println("プレイヤ１のビッドはエラーです");
			info.my_money = 0;
			info.opponent_money = 60;
			dos1.writeInt(6);
			dos2.writeInt(7);
		
			break;
		}

	//プレイヤ２のビッドエラー	
		if(info.opponent_bid == 0 || info.opponent_bid > info.my_money || info.opponent_bid > info.opponent_money)
		{
			System.err.println("プレイヤ２のビッドはエラーです");
			info.my_money = 60;
			info.opponent_money = 0;
			dos1.writeInt(7);
			dos2.writeInt(6);

			break;
		}
		

	//それぞれの相手のビッド額送信
		dos1.writeInt(info.opponent_bid);
		dos2.writeInt(info.my_bid);
		

	//コールｏｒドロップ質問と賭け金処理
		if (info.my_bid > info.opponent_bid)	//プレイヤ１の賭け金の方が大きいとき
		{

		//プレイヤ２にコールｏｒドロップを質問、プレイヤ１には待ってもらう	
			dos2.writeInt(1);
			dos1.writeInt(2);
		
		//コールorドロップ宣言受信
			decision = dis2.readUTF();

		//宣言エラーチェック
			if (!decision.equals("c") && !decision.equals("d"))
			{	
				System.err.println("プレイヤ２の宣言はエラーです");
				info.my_money = 60;
				info.opponent_money = 0;
				dos1.writeInt(7);
				dos2.writeInt(6);
		
				break;
			}
	
			info.opponent_decision = decision.charAt(0);
			info.my_decision = 'n';

		//プレイヤ２がコールした
			if (info.opponent_decision == 'c') {
				System.out.println("プレイヤ２はコールしました");
				dos1.writeInt(1);
				dos2.writeInt(1);
				
		//賭け金処理
				if (info.my_card == 2 && info.opponent_card == 14) {
					System.out.println("プレイヤ１が勝ちました");
					info.my_money += info.my_bid;
					info.opponent_money -= info.my_bid;
				} else if (info.my_card == 14 && info.opponent_card == 2) {
					System.out.println("プレイヤ２が勝ちました");
					info.my_money -= info.my_bid;
					info.opponent_money += info.my_bid;
				} else if (info.my_card > info.opponent_card) {
					System.out.println("プレイヤ１が勝ちました");
					info.my_money += info.my_bid;
					info.opponent_money -= info.my_bid;
				} else if (info.my_card < info.opponent_card) {
					System.out.println("プレイヤ２が勝ちました");
					info.my_money -= info.my_bid;
					info.opponent_money += info.my_bid;
				} else {
					System.out.println("引き分けです");
				}
										
		//プレイヤ２がドロップした & 賭け金処理
			} else if (info.opponent_decision == 'd'){
				System.out.println("プレイヤ２はドロップしました");
				dos1.writeInt(2);
				dos2.writeInt(2);
				System.out.println("プレイヤ１が勝ちました");
				info.my_money += info.opponent_bid;
				info.opponent_money -= info.opponent_bid;

			}


		}
		else if (info.my_bid < info.opponent_bid)	//プレイヤ２の賭け金の方が大きいとき
		{

		//プレイヤ１にコールｏｒドロップを質問、プレイヤ２には待ってもらう
	
			dos1.writeInt(1);
			dos2.writeInt(2);

		//コールorドロップ宣言受信
			decision = dis1.readUTF();

		//宣言エラーチェック
			if (!decision.equals("c") && !decision.equals("d"))
			{	
				System.err.println("プレイヤ１の宣言はエラーです");
				info.my_money = 0;
				info.opponent_money = 60;
				dos1.writeInt(6);
				dos2.writeInt(7);
		
				break;
			}

			info.my_decision = decision.charAt(0);
			info.opponent_decision = 'n';

		//プレイヤ１がコールした
			if (info.my_decision == 'c') {
				System.out.println("プレイヤ１はコールしました");
				dos2.writeInt(1);
				dos1.writeInt(1);

		//賭け金処理
				if (info.my_card == 2 && info.opponent_card == 14) {
					System.out.println("プレイヤ１が勝ちました");
					info.my_money += info.opponent_bid;
					info.opponent_money -= info.opponent_bid;
				} else if (info.my_card == 14 && info.opponent_card == 2) {
					System.out.println("プレイヤ２が勝ちました");
					info.my_money -= info.opponent_bid;
					info.opponent_money += info.opponent_bid;
				} else if (info.my_card > info.opponent_card) {
					System.out.println("プレイヤ１が勝ちました");
					info.my_money += info.opponent_bid;
					info.opponent_money -= info.opponent_bid;
				} else if (info.my_card < info.opponent_card) {
					System.out.println("プレイヤ２が勝ちました");
					info.my_money -= info.opponent_bid;
					info.opponent_money += info.opponent_bid;
				} else {
					System.out.println("引き分けです");
				}

		//プレイヤ１がドロップした & 賭け金処理
			} else if (info.my_decision == 'd'){
				System.out.println("プレイヤ１はドロップしました");
				dos2.writeInt(2);
				dos1.writeInt(2);
				System.out.println("プレイヤ２が勝ちました");
				info.my_money -= info.my_bid;
				info.opponent_money += info.my_bid;
		
			}
		}
		else	//賭け金が同じとき
		{
			dos1.writeInt(3);
			dos2.writeInt(3);

		//賭け金処理 || 
			if (info.my_card == 2 && info.opponent_card == 14) {
				System.out.println("プレイヤ１が勝ちました");
				info.my_money += info.my_bid;
				info.opponent_money -= info.my_bid;
			} else if (info.my_card == 14 && info.opponent_card == 2) {
				System.out.println("プレイヤ２が勝ちました");
				info.my_money -= info.my_bid;
				info.opponent_money += info.my_bid;
			} else if (info.my_card > info.opponent_card) {
				System.out.println("プレイヤ１が勝ちました");
				info.my_money += info.my_bid;
				info.opponent_money -= info.my_bid;
			} else if (info.my_card < info.opponent_card) {
				System.out.println("プレイヤ２が勝ちました");
				info.my_money -= info.my_bid;
				info.opponent_money += info.my_bid;
			} else {
				System.out.println("引き分けです");
			}

		}
		
		System.out.println("プレイヤ１の所持金 " + info.my_money 
+ "\n" + "プレイヤ２の所持金 " + info.opponent_money + "\n");

	//自分のカード、現在の自分の所持金、相手の所持金を送信
		dos1.writeInt(info.my_card);
		dos2.writeInt(info.opponent_card);
		dos1.writeInt(info.my_money);
		dos2.writeInt(info.opponent_money);
		dos1.writeInt(info.opponent_money);
		dos2.writeInt(info.my_money);
		

	//どちらかの所持金が0になるか10000回ゲームをしたら終了
		if (info.my_money <= 0 || info.opponent_money <= 0 || game_num > 9999)
		{
			game = false;
		}

	//ゲームの続行ｏｒ終了を送信
		dos1.writeBoolean(game);
		dos2.writeBoolean(game);

	//ゲーム数カウント
		game_num++;

	}
	
	//勝利者判定＆メッセージ
		System.out.println("");
		if (info.my_money > info.opponent_money) 
			System.out.println("プレイヤ１の勝利です!!");
		else if (info.my_money < info.opponent_money) 
			System.out.println("プレイヤ２の勝利です!!");
		else System.out.println("引き分けです");

		System.out.println("ゲーム終了です");

	//クライアントからの接続終了メッセージ
		System.out.println("プレイヤ１の" + dis1.readUTF());
		System.out.println("プレイヤ２の" + dis2.readUTF());

	//ソケットクローズ
		s1.close();
		s2.close();

                ss.close();

	}
	catch (Exception e) {
		System.out.println("Exception: " + e);
	}

}

//カード表示用メソッド
	static String card(int i){
		String s;
		switch(i){
			case 11:
				s = "J";
				break;
			case 12:
				s = "Q";
				break;
			case 13:
				s = "K";
				break;		
			case 14:
				s = "A";
				break;
			default:
				s = "" + i;
		}

	return s;
	}

}

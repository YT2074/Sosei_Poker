/* クライアントプログラム */

// ライブラリの利用
//import java.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

public static void main(String args[]) {

	int play_mode = 0;	//プレイモード(0:コンピュータ知識　1:人間入力)
	int portnum = 0;				//接続ポートNo.
	String servname          = new String();	//サーバの名前
	String pnumber           = new String();	//ポートNo.用

	String bid;		//ビッド額用
	String decision;	//コールorドロップ宣言用

	int x;			//命令受信用
	int i;			//命令受信用

	boolean game = true;	//ゲーム終了判定用
	boolean game2 = false;	//ゲーム終了判定用

	int money;		//勝敗決定用(前回の自分の所持金）
	int money1;		//previous 保存用
	int money2;		//previous 保存用
	int game_num = 1;	//ゲーム数カウント

	KnowledgeClass know = new KnowledgeClass();	//知識クラス

	know.previous.my_money = 30;
	know.previous.opponent_money = 30;
	know.current.my_money = 30;
	know.current.opponent_money = 30;


//プレイモードチェック（java Client -p で人間入力モード）
	if(args.length > 0)
	{
		if(args[0].equals("-p"))
			play_mode = 1;
	}

	try {

	//サーバの名前入力
		System.err.println("サーバの名前を入力して下さい");
		InputStreamReader sin2 = new InputStreamReader(System.in);
		BufferedReader sin3 = new BufferedReader(sin2);
		servname = sin3.readLine();

	//ポート番号入力
		System.err.println("ポート番号を入力して下さい");
		pnumber = sin3.readLine();
		portnum = Integer.parseInt(pnumber);

		System.out.println("Server Name:" + servname + "\n"
			     + "Port Number:" + portnum);
	}

// 例外処理
	catch(Exception e) {
		System.err.println("入力エラーです．");
    		System.exit(1);
	}


	try{

	//ソケット作成
		Socket s = new Socket(servname,portnum);

	//ストリーム準備＆参加登録
		OutputStream os = s.getOutputStream();
		DataOutputStream dos = new DataOutputStream(os);

		InputStream is = s.getInputStream();
		DataInputStream dis = new DataInputStream(is);
		String str = dis.readUTF();
		System.out.println(str);

	//ゲーム開始
		while(game) {

			System.out.println("\n" + game_num + " ゲーム目開始!!");

		//初期化
			know.current.opponent_card = 0;
			know.current.my_card = 0;
			know.current.opponent_bid = 0;
			know.current.my_bid = 0;
			know.current.opponent_decision = 'n';
			know.current.my_decision = 'n';
			bid = "";
			decision = "n";

		//相手カード情報取得
			know.current.opponent_card = dis.readInt();
			System.out.println("相手のカードは " + card(know.current.opponent_card));

		//ビッド数決定
			if(play_mode == 0)
				bid = know.bid();
			else
				bid = Human_bid(Math.min(know.current.my_money, 60-know.current.my_money));

		//自分のビッド数送信
			System.out.println("私は   " + bid + "　ビッド賭けます");
			dos.writeUTF(bid);

		//相手のビッド数受信
			know.current.opponent_bid = dis.readInt();

		//相手のビッド数が6,7,8,のときはエラー -> 終了
			if(know.current.opponent_bid == 6){
				System.err.println("私のビッドはエラーだったようです");
				know.current.my_money = 0;
				know.current.opponent_money = 60;
				break;
			} else if(know.current.opponent_bid == 7){
				System.err.println("相手のビッドはエラーだったようです");
				know.current.my_money = 60;
				know.current.opponent_money = 0;
				break;
			} else if(know.current.opponent_bid == 8){
				System.err.println("二人のビッドはエラーだったようです");
				know.current.my_money = 30;
				know.current.opponent_money = 30;
				break;
			}

			System.out.println("相手は " + know.current.opponent_bid + "　ビッド賭けました");

		//自分のビッド額をcurrentに代入
			know.current.my_bid = Integer.parseInt(bid);

		//コールorドロップまたは待機
		//x=1:コールorドロップの決定、x=2:待機、x=3:ビッド額同一
			x = dis.readInt();

			switch (x)
			{
				case 1:
					System.out.println("コールしますか？ドロップしますか？");

				//コールorドロップ決定関数呼び出し
					if(play_mode == 0)
						decision = know.decision();
					else
						decision = Human_decision();


					if (decision.equals("c")) {
						System.out.println("私はコールします");
					}
					else if (decision.equals("d")){
						System.out.println("私はドロップします");
					}
					dos.writeUTF(decision);

				//自分の宣言がエラーの場合は6が返ってくる
					if(dis.readInt() == 6){
						System.err.println("私の宣言はエラーだったようです");
						know.current.my_money = 0;
						know.current.opponent_money = 60;
						game2 = true;

					}

				//自分の宣言をcurrentに代入
					know.current.my_decision = decision.charAt(0);

				break;

				case 2:
					System.out.println("相手のコールorドロップの決定待ちです");

				//i=1:相手がコール　i=2:相手がドロップ　i=6:相手の宣言エラー
					i = dis.readInt();
					if(i == 1) {
						System.out.println("相手はコールしました");
						know.current.opponent_decision = 'c';
					} else if(i == 2) {
						System.out.println("相手はドロップしました");
						know.current.opponent_decision = 'd';
					} else {
						System.err.println("相手の宣言はエラーだったようです");
						know.current.my_money = 60;
						know.current.opponent_money = 0;
						game2 = true;
					}

				break;

				case 3:
					System.out.println("ビッド額は同じです");
				break;

				default:
					System.err.println("エラーです");
			}

			if(game2) break;


		//自分のカード情報受信
			know.current.my_card = dis.readInt();
			System.out.println("自分のカードは" + card(know.current.my_card) + "でした");
		//勝敗決定用に代入
			money = know.current.my_money;

		//自分の所持金、相手の所持金受信
			money1 = know.current.opponent_money;
			money2 = know.current.my_money;

			know.current.my_money = dis.readInt();
			know.current.opponent_money = dis.readInt();

		//勝敗表示
			if(know.current.my_money > money){
				System.out.println("勝ちました");
			} else if(know.current.my_money < money){
				System.out.println("負けました");
			} else {
				System.out.println("引き分けでした");
			}

			System.out.println("自分の所持金 " + know.current.my_money
+ "\n相手の所持金 " + know.current.opponent_money);

		//ゲーム情報をpreviousにコピー
			know.previous.opponent_money = money1;
			know.previous.my_money = money2;
			know.previous.opponent_card = know.current.opponent_card;
			know.previous.my_card = know.current.my_card;
			know.previous.opponent_bid = know.current.opponent_bid;
			know.previous.my_bid = know.current.my_bid;
			know.previous.opponent_decision = know.current.opponent_decision;
			know.previous.my_decision = know.current.my_decision;

			//直前のゲーム情報をlog.txtに保存
					try{
			   		File file = new File("log"+pnumber+".txt");

			   		if (checkBeforeWritefile(file)){
							PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file,true))); //ログファイルに追記
					   	pw.print(know.previous.opponent_money);
					   	pw.print(',');
					   	pw.print(know.previous.my_money);
					   	pw.print(',');
					   	pw.print(know.previous.opponent_card);
					   	pw.print(',');
					   	pw.print(know.previous.my_card);
							pw.print(',');
					   	pw.print(know.previous.opponent_bid);
							pw.print(',');
					   	pw.print(know.previous.my_bid);
							pw.print(',');
					   	pw.print(know.previous.opponent_decision);
							pw.print(',');
					   	pw.println(know.previous.my_decision);
						 	pw.close();
			     		}else{
			       	  System.out.println("Error: cannot open file");
			      	}
			    	}catch(IOException e){
							System.out.println("IO Exception");
						}
		//ゲーム終了判定情報受信
			game = dis.readBoolean();

		//ゲーム数カウント
			game_num++;

		}

	//最終勝敗判定
		System.out.println("");
		if(know.current.my_money > 30)
			System.out.println("勝利です!!");
		else if (know.current.my_money < 30)
			System.out.println("敗北です…");
		else System.out.println("引き分けです");

		System.out.println("ゲーム終了です");

	//接続終了
		dos.writeUTF("接続を切ります");
	//ソケットクローズ
		s.close();

	}
	catch (Exception e) {
		System.out.println("ネットワークエラー" + e);
	}



}




//人間のビッド数の決定
	static String Human_bid(int money){

		String str = "";
		InputStreamReader sin2 = new InputStreamReader(System.in);
		BufferedReader sin3 = new BufferedReader(sin2);

        	boolean check = true;
		while(check)
		{
			System.err.print("ビッド数を決定してください(1〜5) -> ");
			try {

				str = sin3.readLine();
				if((str.equals("1") || str.equals("2") || str.equals("3") || str.equals("4") || str.equals("5")) && Integer.parseInt(str) <= money) {

				check = false;

				} else {
					System.err.print("入力エラーです！");
				}
			} catch (IOException e){
				System.out.println("IO Exception");
				System.exit(1);
			}

		}

		return str;
    	}


	/* 人間のコール or ドロップの決定 */
	static String Human_decision()
	{
		String decision = "n";
		InputStreamReader sin2 = new InputStreamReader(System.in);
		BufferedReader sin3 = new BufferedReader(sin2);

        	boolean check = true;
		while(check)
		{
			System.err.print("コール(Type c) or ドロップ(Type d) ->");
			try {
				decision = sin3.readLine();
				if(decision.equals("c") || decision.equals("d")){
					check = false;
				} else {
					System.err.print("入力エラーです！");
				}

			} catch (IOException e){
				System.err.println(e);
				System.exit(1);
			}
		}
		return decision;
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

	//ファイルの書き込み事前確認（ファイルが無ければ作成する）
	private static boolean checkBeforeWritefile(File file){
    if (file.exists()){
      if (file.isFile() && file.canWrite()){
        return true;
      }
    }else{
			try{
    		file.createNewFile();
				if (file.isFile() && file.canWrite()){
        	return true;
      	}
			}catch(IOException e){
				return false;
		}
		}
    return false;
  }

}

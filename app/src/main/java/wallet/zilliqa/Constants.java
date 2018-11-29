package wallet.zilliqa;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Constants {
  private Constants() {
  }

  public static String publicAddress = "";
  public static String lastScanAddress = "";
  public static final String TESTADDRESS = "0x000000591672c2Ad77D99f62BE38Eb2C995bb09c"; // used to autofill when app is in debug mode

  public static String getBlockchanPreference(Context ctx) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    String networkPreference = prefs.getString("network_preference", "");

    switch (networkPreference) {
      case "mainnet":
        return "https://mainnet.infura.io/";
      case "testnet":
        return "https://rinkeby.infura.io/";
      default:
        return "https://rinkeby.infura.io/\"";
    }
  }

  public static String getExplorerUrl(Context ctx) {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    String networkPreference = prefs.getString("network_preference", "");

    switch (networkPreference) {
      case "mainnet":
        return "https://explorer-scilla.zilliqa.com/transactions/";
      case "testnet":
        return "https://explorer-scilla.zilliqa.com/transactions/";
      default:
        return "https://explorer-scilla.zilliqa.com/transactions/";
    }
  }

  public static DecimalFormat getDecimalFormat() {
    DecimalFormat df = new DecimalFormat("#.########");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df;
  }
}

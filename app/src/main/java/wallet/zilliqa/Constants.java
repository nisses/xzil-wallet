package wallet.zilliqa;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class Constants {
  private Constants() {
  }

  // I know you can see them, please don't drain them of fake money :)
  public static String newWalletPublicAddress = "573EC96638C8BB1C386394602E1460634F02ADDD";
  public static String newWalletPrivateKey = "69122B6C3A70B6CC7908546B7F6233F1F5501ECC5759D2940E32FEE250E7AA7A";
  public static String newWalletPublicAddress2 = "37182D3C7033D76080E04F70BED76A0496D14CD1";
  public static String newWalletPrivateKey2 = "BD04FFE30016A975B4193468A54A852F58F10E913BE8945C1C33CB5EA0CF5881";
  public static String newWalletSeed = "spike bleak robust note resist typical unfair domain easy cousin similar sister"; //f03ececc92a157aa4c3f26de32069749f7325bac
  public static String newWalletSeed2 = "will grow market harsh elbow divert keep course absorb brass phrase lucky";  //136df6c4ce41113e77e2e858ffc793be9b74ffbd

  //------- another test account -------------
  public static String newWalletPrivateKeyX = "0x82163Ce6f719fBc5Bf3e4280Bb23C4c413172eE6";
  // Parola123! -> spike bleak robust note resist typical unfair domain easy cousin similar sister -> 0x82163Ce6f719fBc5Bf3e4280Bb23C4c413172eE6
  //----------------------------------------------------------------------------------------------------------------------

  public static String publicAddress = "";
  public static String lastScanAddress = "";

  public static final String EXPLORER_URL = "https://viewblock.io/zilliqa/tx/";



  public static DecimalFormat getDecimalFormat() {
    DecimalFormat df = new DecimalFormat("#.########");
    df.setRoundingMode(RoundingMode.FLOOR);
    return df;
  }
}

package wallet.zilliqa.utils.crypto;

import com.firestack.laksaj.account.Account;
import com.firestack.laksaj.utils.ByteUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public class Bech32 {


  public static boolean isBech32(String str) {
    Pattern pattern = Pattern.compile("^zil1[qpzry9x8gf2tvdw0s3jn54khce6mua7l]{38}");
    Matcher matcher = pattern.matcher(str);
    return matcher.matches();
  }

  /**
   * The Bech32 character set for encoding.
   */
  private static final String CHARSET = "qpzry9x8gf2tvdw0s3jn54khce6mua7l";

  /**
   * The Bech32 character set for decoding.
   */
  private static final byte[] CHARSET_REV = {
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
      15, -1, 10, 17, 21, 20, 26, 30, 7, 5, -1, -1, -1, -1, -1, -1,
      -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
      1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1,
      -1, 29, -1, 24, 13, 25, 9, 8, 23, -1, 18, 22, 31, 27, 19, -1,
      1, 0, 3, 16, 11, 28, 12, 14, 6, 4, 2, -1, -1, -1, -1, -1
  };
  public static String HRP = "zil";

  /**
   * Find the polynomial with value coefficients mod the generator as 30-bit.
   */
  private static int polymod(final byte[] values) {
    int c = 1;
    for (byte v_i : values) {
      int c0 = (c >>> 25) & 0xff;
      c = ((c & 0x1ffffff) << 5) ^ (v_i & 0xff);
      if ((c0 & 1) != 0) c ^= 0x3b6a57b2;
      if ((c0 & 2) != 0) c ^= 0x26508e6d;
      if ((c0 & 4) != 0) c ^= 0x1ea119fa;
      if ((c0 & 8) != 0) c ^= 0x3d4233dd;
      if ((c0 & 16) != 0) c ^= 0x2a1462b3;
    }
    return c;
  }

  /**
   * Expand a HRP for use in checksum computation.
   */
  private static byte[] expandHrp(final String hrp) {
    int hrpLength = hrp.length();
    byte[] ret = new byte[hrpLength * 2 + 1];
    for (int i = 0; i < hrpLength; ++i) {
      int c = hrp.charAt(i) & 0x7f; // Limit to standard 7-bit ASCII
      ret[i] = (byte) ((c >>> 5) & 0x07);
      ret[i + hrpLength + 1] = (byte) (c & 0x1f);
    }
    ret[hrpLength] = 0;
    return ret;
  }

  /**
   * Verify a checksum.
   */
  private static boolean verifyChecksum(final String hrp, final byte[] values) {
    byte[] hrpExpanded = expandHrp(hrp);
    byte[] combined = new byte[hrpExpanded.length + values.length];
    System.arraycopy(hrpExpanded, 0, combined, 0, hrpExpanded.length);
    System.arraycopy(values, 0, combined, hrpExpanded.length, values.length);
    return polymod(combined) == 1;
  }

  /**
   * Create a checksum.
   */
  private static byte[] createChecksum(final String hrp, final byte[] values) {
    byte[] hrpExpanded = expandHrp(hrp);
    byte[] enc = new byte[hrpExpanded.length + values.length + 6];
    System.arraycopy(hrpExpanded, 0, enc, 0, hrpExpanded.length);
    System.arraycopy(values, 0, enc, hrpExpanded.length, values.length);
    int mod = polymod(enc) ^ 1;
    byte[] ret = new byte[6];
    for (int i = 0; i < 6; ++i) {
      ret[i] = (byte) ((mod >>> (5 * (5 - i))) & 31);
    }
    return ret;
  }

  /**
   * Encode a Bech32 string.
   */
  public static String encode(final Bech32Data bech32) {
    return encode(bech32.hrp, bech32.data);
  }

  /**
   * Encode a Bech32 string.
   */
  public static String encode(String hrp, final byte[] values) {
    checkArgument(hrp.length() >= 1, "Human-readable part is too short");
    checkArgument(hrp.length() <= 83, "Human-readable part is too long");
    hrp = hrp.toLowerCase(Locale.ROOT);
    byte[] checksum = createChecksum(hrp, values);
    byte[] combined = new byte[values.length + checksum.length];
    System.arraycopy(values, 0, combined, 0, values.length);
    System.arraycopy(checksum, 0, combined, values.length, checksum.length);
    StringBuilder sb = new StringBuilder(hrp.length() + 1 + combined.length);
    sb.append(hrp);
    sb.append('1');
    for (byte b : combined) {
      sb.append(CHARSET.charAt(b));
    }
    return sb.toString();
  }

  /**
   * Decode a Bech32 string.
   */
  public static Bech32Data decode(final String str) throws AddressFormatException {
    boolean lower = false, upper = false;
    if (str.length() < 8) {
      throw new AddressFormatException.InvalidDataLength("Input too short: " + str.length());
    }
    if (str.length() > 90) {
      throw new AddressFormatException.InvalidDataLength("Input too long: " + str.length());
    }
    for (int i = 0; i < str.length(); ++i) {
      char c = str.charAt(i);
      if (c < 33 || c > 126) throw new AddressFormatException.InvalidCharacter(c, i);
      if (c >= 'a' && c <= 'z') {
        if (upper) {
          throw new AddressFormatException.InvalidCharacter(c, i);
        }
        lower = true;
      }
      if (c >= 'A' && c <= 'Z') {
        if (lower) {
          throw new AddressFormatException.InvalidCharacter(c, i);
        }
        upper = true;
      }
    }
    final int pos = str.lastIndexOf('1');
    if (pos < 1) throw new AddressFormatException.InvalidPrefix("Missing human-readable part");
    final int dataPartLength = str.length() - 1 - pos;
    if (dataPartLength < 6) {
      throw new AddressFormatException.InvalidDataLength("Data part too short: " + dataPartLength);
    }
    byte[] values = new byte[dataPartLength];
    for (int i = 0; i < dataPartLength; ++i) {
      char c = str.charAt(i + pos + 1);
      if (CHARSET_REV[c] == -1) throw new AddressFormatException.InvalidCharacter(c, i + pos + 1);
      values[i] = CHARSET_REV[c];
    }
    String hrp = str.substring(0, pos).toLowerCase(Locale.ROOT);
    if (!verifyChecksum(hrp, values)) throw new AddressFormatException.InvalidChecksum();
    return new Bech32Data(hrp, Arrays.copyOfRange(values, 0, values.length - 6));
  }

  public static List<Integer> convertBits(byte[] data, int fromWidth, int toWidth, boolean pad) {
    int acc = 0;
    int bits = 0;
    int maxv = (1 << toWidth) - 1;
    List<Integer> ret = new ArrayList<>();

    for (int i = 0; i < data.length; i++) {
      int value = data[i] & 0xff;
      if (value < 0 || value >> fromWidth != 0) {
        return null;
      }
      acc = (acc << fromWidth) | value;
      bits += fromWidth;
      while (bits >= toWidth) {
        bits -= toWidth;
        ret.add((acc >> bits) & maxv);
      }
    }

    if (pad) {
      if (bits > 0) {
        ret.add((acc << (toWidth - bits)) & maxv);
      } else if (bits >= fromWidth || ((acc << (toWidth - bits)) & maxv) != 0) {
        return null;
      }
    }

    return ret;
  }

  public static String toBech32Address(String address) throws Exception {
    if (!XZilValidation.isAddress(address)) {
      throw new Exception("Invalid address format.");
    }

    address = address.toLowerCase().replace("0x", "");

    List<Integer> bits = convertBits(ByteUtil.hexStringToByteArray(address), 8, 5, false);

    byte[] addrBz = new byte[bits.size()];

    for (int i = 0; i < bits.size(); i++) {
      addrBz[i] = bits.get(i).byteValue();
    }

    if (null == addrBz) {
      throw new Exception("Could not convert byte Buffer to 5-bit Buffer");
    }

    return encode(HRP, addrBz);
  }

  public static String fromBech32Address(String address) throws Exception {
    Bech32Data data = decode(address);

    if (!data.hrp.equals(HRP)) {
      throw new Exception("Expected hrp to be zil");
    }

    List<Integer> bits = convertBits(data.data, 5, 8, false);
    byte[] buf = new byte[bits.size()];
    for (int i = 0; i < bits.size(); i++) {
      buf[i] = bits.get(i).byteValue();
    }
    if (null == buf || buf.length == 0) {
      throw new Exception("Could not convert buffer to bytes");
    }

    return Account.toCheckSumAddress(ByteUtil.byteArrayToHexString(buf)).replace("0x", "");
  }

  public static class Bech32Data {
    public final String hrp;
    public final byte[] data;

    private Bech32Data(final String hrp, final byte[] data) {
      this.hrp = hrp;
      this.data = data;
    }
  }
}

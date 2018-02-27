package jp.sourceforge.reedsolomon;

/**
 * タイトル: RSコード�?�エンコー�?
 *
 * @author Masayuki Miyazaki
 * http://sourceforge.jp/projects/reedsolomon/
 */
public class RsEncode {
	public static final int RS_PERM_ERROR = -1;
	private static final Galois galois = Galois.getInstance();
	private int npar;
	private int[] encodeGx;

	public RsEncode(int npar) {
		this.npar = npar;
		makeEncodeGx();
	}

	/**
	 * 生�?�多�??式�?��?��?�作�??
	 *		G(x)=�?[k=0,n-1](x + α^k)
	 *		encodeGxの添え字と次数の並びが�??なのに注�?
	 *		encodeGx[0]        = x^(npar - 1)の�?
	 *		encodeGx[1]        = x^(npar - 2)の�?
	 *		...
	 *		encodeGx[npar - 1] = x^0の�?
	 */
	private void  makeEncodeGx() {
		encodeGx = new int[npar];
		encodeGx[npar - 1] = 1;
		for(int kou = 0; kou < npar; kou++) {
			int ex = galois.toExp(kou);			// ex = α^kou
			// (x + α^kou)を掛�?
			for(int i = 0; i < npar - 1; i++) {
				// 現在の�? * α^kou + �?つ下�?�次数の�?
				encodeGx[i] = galois.mul(encodeGx[i], ex) ^ encodeGx[i + 1];
			}
			encodeGx[npar - 1] = galois.mul(encodeGx[npar - 1], ex);		// �?下位�??の計�?
		}
	}

	/**
	 * RSコード�?�エンコー�?
	 *
	 * @param data int[]
	 *		入力データ配�??
	 * @param length int
	 *		入力データ長
	 * @param parity int[]
	 *		パリ�?ィ格納用配�??
	 * @param parityStartPos int
	 *		パリ�?ィ格納用Index
	 * @return int
	 *		0 : ok
	 *		< 0: エラー
	 */
	public int encode(int[] data, int length, int[] parity, int parityStartPos)	{
		if(length < 0 || length + npar > 255) {
			return RS_PERM_ERROR;
		}

		/*
		 * パリ�?ィ格納用配�??
		 * wr[0]        �?上�?
		 * wr[npar - 1] �?下�?		なのに注�?
		 * これでパリ�?ィを�??�?に並べかえなくてよいので、arraycopyが使える
		 */
		int[] wr = new int[npar];

		for(int idx = 0; idx < length; idx++) {
			int code = data[idx];
			int ib = wr[0] ^ code;
			for(int i = 0; i < npar - 1; i++) {
				wr[i] = wr[i + 1] ^ galois.mul(ib, encodeGx[i]);
			}
			wr[npar - 1] = galois.mul(ib, encodeGx[npar - 1]);
		}
		if(parity != null) {
			System.arraycopy(wr, 0, parity, parityStartPos, npar);
		}
		return 0;
	}

	public int encode(int[] data, int length, int[] parity)	{
		return encode(data, length, parity, 0);
	}

	public int encode(int[] data, int[] parity)	{
		return encode(data, data.length, parity, 0);
	}

/*
	public static void main(String[] args) {
		int[] data = new int[] {32, 65, 205, 69, 41, 220, 46, 128, 236};
		int[] parity = new int[17];
		RsEncode enc = new RsEncode(17);
		enc.encode(data, parity);
		System.out.println(java.util.Arrays.toString(parity));
		System.out.println("[42, 159, 74, 221, 244, 169, 239, 150, 138, 70, 237, 85, 224, 96, 74, 219, 61]");
	}
*/
}

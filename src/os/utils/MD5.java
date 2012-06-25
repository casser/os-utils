package os.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class MD5 {

	public static byte[] random(){
		UUID ra = UUID.randomUUID();
		long lb = ra.getLeastSignificantBits();
		long mb = ra.getMostSignificantBits();
		return new byte[]{
			((byte)((lb >> 0 ) & 0xFF)),
			((byte)((lb >> 8 ) & 0xFF)),
			((byte)((lb >> 16) & 0xFF)),
			((byte)((lb >> 24) & 0xFF)),
			((byte)((lb >> 32) & 0xFF)),
			((byte)((lb >> 40) & 0xFF)),
			((byte)((lb >> 48) & 0xFF)),
			((byte)((lb >> 56) & 0xFF)),
			((byte)((mb >> 0 ) & 0xFF)),
			((byte)((mb >> 8 ) & 0xFF)),
			((byte)((mb >> 16) & 0xFF)),
			((byte)((mb >> 24) & 0xFF)),
			((byte)((mb >> 32) & 0xFF)),
			((byte)((mb >> 40) & 0xFF)),
			((byte)((mb >> 48) & 0xFF)),
			((byte)((mb >> 56) & 0xFF))
		};
	}
	
	public static String hash(){
		UUID ra = UUID.randomUUID();
		long lb = ra.getLeastSignificantBits();
		long mb = ra.getMostSignificantBits();
		return Long.toHexString(lb)+Long.toHexString(mb);
	}
	
	public static String hash(String input){
		if(input.length()==32 && input.toLowerCase().matches("^[a-f0-9]{32}$")){
			return input;
		}else{
	        byte[] ra 	= bytes(input,true);
	        long lb 	= BytesUtil.readLong(ra, 0);
			long mb 	= BytesUtil.readLong(ra, 4);
			return Long.toHexString(lb)+Long.toHexString(mb);
		}
	}
	
	public static byte[] bytes(String input){
		return bytes(input,false);
	}
	
	public static byte[] bytes(String input, Boolean strict){
		if(!strict && input.length()==32 && input.toLowerCase().matches("^[a-f0-9]{32}$")){
			return BytesUtil.fromHex(input);
		}
        return bytes(input.getBytes());
	}

	public static byte[] bytes(byte[] buffer) {
		return bytes(buffer, 0, buffer.length);
	}
	
	public static byte[] bytes(byte[] buffer, int offset, int length) {
		try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(buffer,offset,length);
           	return md.digest();
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
	}
}

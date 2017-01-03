
import java.security.*;

public class SimpleEditor
{
	public static void main( String args[] )
	{
		try
		{
			String yourString = "Aa";
			byte[] bytesOfMessage = yourString.getBytes("UTF-8");
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] thedigest = md.digest(bytesOfMessage);
			String s = new String(thedigest);
			String g = s;
			System.out.println(s.equals(g));
			System.out.println(yourString.hashCode());
			String h = "BB";
			System.out.println(h.hashCode());
		}
		catch(Exception e)
		{
			
		}
	}
}
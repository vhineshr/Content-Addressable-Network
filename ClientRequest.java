import java.io.DataOutputStream;
import java.net.Socket;


public class ClientRequest 
{
	public static void main(String args[])
	{
		new ClientServer().start();
		String cacheIP = "129.21.156.83";
		String fileName = "TextDoc.txt";
		
		try
		{
			Socket socket = new Socket( cacheIP  , 10000);
			DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
			dataOutputStream.writeBytes( fileName + "\n" ); 
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}

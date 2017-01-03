import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;


public class ClientServer extends Thread
{
	ServerSocket listener; 
	
	public ClientServer() 
	{
		try
		{
			listener = new ServerSocket(10001);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			Socket socket = listener.accept();
			BufferedReader bReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter("TextDoc.txt") );
			
			String line = "";
			String text = "";
			while( !( line = bReader.readLine() ).equals(")))")  )
			{
				System.out.println(line);
				text = text + line + "\n";
			}
			bufferedWriter.write( text );
			bufferedWriter.flush();
			bufferedWriter.close();
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

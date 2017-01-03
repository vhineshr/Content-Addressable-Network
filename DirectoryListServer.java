import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;

class DirectoryList
{
	String FileName;
	ArrayList<String> IPList ;
	
	public DirectoryList( String FileName  ) 
	{
		this.FileName = FileName;
		IPList = new ArrayList<String>();
	}
	
	
}

class DirectoryListServerResponse extends Thread
{
	
	Socket socket; 
	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;
	String fileName;
	Zone requestedPointInZone;
	int belongedToIndex;
	ArrayList<DirectoryList> directoryLists;
	
	public DirectoryListServerResponse( ArrayList<NeighbourPeer> neighbourPeers , ArrayList<Zone> belongedZones ,String fileName , Socket socket , ArrayList<DirectoryList> directoryLists) 
	{
		this.socket = socket;
		this.neighbourPeers = neighbourPeers;
		this.belongedZones = belongedZones;
		this.fileName = fileName;
		this.directoryLists = directoryLists;
	}
	
	boolean isMyZone(String zone) {
		String zoneVal[] = zone.split(" ");
		int x = Integer.parseInt(zoneVal[0]);
		int y = Integer.parseInt(zoneVal[1]);
		boolean boolVal = false;
		for (int i = 0; i < belongedZones.size(); i++) {
			if ((belongedZones.get(i).getTop().getx() <= x)
					&& (belongedZones.get(i).getTop().gety() <= y)
					&& (belongedZones.get(i).getBottom().getx() >= x)
					&& (belongedZones.get(i).getBottom().gety() >= y)) {
				boolVal = true;
				requestedPointInZone = belongedZones.get(i);
				belongedToIndex = i;
			}
		}
		return boolVal;
	}
	
	boolean isNeighbourZone(String zone, Zone zoneObj) {
		String zoneVal[] = zone.split(" ");
		int x = Integer.parseInt(zoneVal[0]);
		int y = Integer.parseInt(zoneVal[1]);
		boolean boolVal = false;
		if ((zoneObj.getTop().getx() <= x) && (zoneObj.getTop().gety() <= y)
				&& (zoneObj.getBottom().getx() >= x)
				&& (zoneObj.getBottom().gety() >= y)) {
			boolVal = true;
		}

		return boolVal;
	}
	
	boolean getIP(String g) {
		String ip = "";
		boolean b = false;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (!addr.getHostAddress().contains(":")) {
						ip = addr.getHostAddress();
					}
				}
			}
			if (ip.equals(g)) {
				return true;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return b;

	}
	
	NeighbourPeer checkTraverse(int belongedZoneNumber, int x, int y,
			String clientIP) {
		System.out.println(clientIP);
		NeighbourPeer nPeer = null;
		int minDistance = 0, count = 0;
		boolean isInMyNeighbour = false;

		for (int j = 0; j < neighbourPeers.size(); j++) {
			isInMyNeighbour = isNeighbourZone(x + " " + y, neighbourPeers
					.get(j).getZone());
			if (isInMyNeighbour == true) {
				nPeer = neighbourPeers.get(j);
				break;
			}
		}
		for (int i = 0; i < neighbourPeers.size(); i++) {

			if (!clientIP.equals(neighbourPeers.get(i).getIPAddress())
					&& !getIP(clientIP)) {

				if (isInMyNeighbour == false) {
					int x1 = neighbourPeers.get(i).getNeighbourTo().getTop()
							.getx();
					int y1 = neighbourPeers.get(i).getNeighbourTo().getTop()
							.gety();
					int x2 = neighbourPeers.get(i).getNeighbourTo().getBottom()
							.getx();
					int y2 = neighbourPeers.get(i).getNeighbourTo().getBottom()
							.gety();

					int midX = (x1 + x2) / 2;
					int midY = (y1 + y2) / 2;

					int distance = ((midX - x) * (midX - x))
							+ ((midY - y) * (midY - y));

					if (minDistance == 0) {
						minDistance = distance;
						count = i;
						nPeer = neighbourPeers.get(i);
					} else {
						if (distance < minDistance) {
							minDistance = distance;
							count = i;
							nPeer = neighbourPeers.get(i);
						}
					}

				}
			}

		}
		System.out.println(count);
		NeighbourPeer temp = neighbourPeers.get(count);
		neighbourPeers
				.set(count, neighbourPeers.get(neighbourPeers.size() - 1));
		neighbourPeers.set(neighbourPeers.size() - 1, temp);
		return nPeer;
	}
	
	public void run()
	{
		try
		{
			BufferedReader bufferedReader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
			String clientMessage = bufferedReader.readLine();
			System.out.println("Message:"+clientMessage);
			String message[] = clientMessage.split("-");
			System.out.println("Message:"+clientMessage);
			System.out.println("Message Length" + message.length);
			String clientIP = socket.getInetAddress().toString();
			clientIP = clientIP.substring(1, clientIP.length());
			if( message.length == 3 )
			{
				if( !message[1].equals("reply") )
				{
					String points[] = message[1].split(" ");
					System.out.println(message[1]);
					System.out.println( isMyZone( message[1] ) );
					if( isMyZone( message[1] ) )
					{
						boolean isFileThere = false;
						for( int i = 0; i < directoryLists.size(); i++  )
						{
							for( int j = 0; j < directoryLists.get(i).IPList.size(); j++ )
							{
								isFileThere = true;
							}
						}
						Socket replySocket = new Socket( message[0] , 10002 );
						DataOutputStream dataOutputStream = new DataOutputStream( replySocket.getOutputStream() );
						if( isFileThere == false )
						{
							dataOutputStream.writeBytes( "0-reply-"+message[2]+"\n" );
						}	
						else
						{
							String ipToGet = "";
							for( int i = 0; i <  directoryLists.size(); i++ )
							{
								if( directoryLists.get(i).FileName.equals("TextDoc.txt") )
								{
									ipToGet = directoryLists.get(i).IPList.get(0);
									directoryLists.get(i).IPList.remove(0);
									directoryLists.get(i).IPList.add(ipToGet);
								}
							}
							dataOutputStream.writeBytes( ipToGet+"-reply-"+message[2]+"\n" );
						}	
					}
					else
					{
						int bx1 = 0, bx2 = 0, by1 = 0, by2 = 0;
						int belongedZoneNumber = 0;

						for (int i = 0; i < neighbourPeers.size(); i++) {
							if (clientIP.equals(neighbourPeers.get(i)
									.getIPAddress())) {
								bx1 = neighbourPeers.get(i)
									.getNeighbourTo().getTop().getx();
								bx2 = neighbourPeers.get(i)
									.getNeighbourTo().getBottom()
									.getx();
								by1 = neighbourPeers.get(i)
									.getNeighbourTo().getTop().gety();
								by2 = neighbourPeers.get(i)
									.getNeighbourTo().getBottom()
									.gety();
							}
						}

						for (int i = 0; i < belongedZones.size(); i++) {
							if (belongedZones.get(i).getTop().getx() == bx1
									&& belongedZones.get(i).getTop().gety() == by1
									&& belongedZones.get(i).getBottom()
											.getx() == bx2
									&& belongedZones.get(i).getBottom()
											.gety() == by2) {
									belongedZoneNumber = i;
							}
						}
						System.out.println(Integer.parseInt(points[0]) + " " + Integer.parseInt(points[1]));
						NeighbourPeer nPeer = checkTraverse( belongedZoneNumber, Integer.parseInt(points[0]) , Integer.parseInt(points[1]), clientIP);
						Socket nSocket = new Socket(nPeer.getIPAddress(), 10002);
						DataOutputStream d = new DataOutputStream( nSocket.getOutputStream() );
						d.writeBytes(clientMessage + "\n");

						nSocket.close();
					}
				
				}
				else
				{
					if(message[0].equals("0"))
					{
						Socket getFile = new Socket( "129.21.30.38" , 10010 );
						
						BufferedReader bReader = new BufferedReader( new InputStreamReader( getFile.getInputStream() ) );
						BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter("TextDoc.txt") );
						
						String line = "";
						String text = "";
						while( !( line = bReader.readLine() ).equals(")))")  )
						{
							System.out.println(line);
							text = text + line + "\n";
						}
						bufferedWriter.write(text);
						bufferedWriter.flush();
						bufferedWriter.close();
						
						
						Socket socketSend = new Socket( clientIP , 10002 );
						System.out.println(clientIP);
						DataOutputStream dataOutputStream = new DataOutputStream( socketSend.getOutputStream() );
					    dataOutputStream.writeBytes( "add-TextDoc.txt" + "\n" );
					    socketSend.close();
					    System.out.println("Added me in ");
					    
					    Socket sendFile = new Socket( message[2] , 10001);
						BufferedReader bufferedReade = new BufferedReader( new FileReader( "TextDoc.txt" ) );
						DataOutputStream dpsrtm = new DataOutputStream( sendFile.getOutputStream() );
						String lin = "";
						while( ( lin = bufferedReade.readLine() ) != null )
						{
							dpsrtm.writeBytes( lin + "\n" );
						}
						dpsrtm.writeBytes( ")))" + "\n" );
						sendFile.close();
						
						HearForFile.fileName = "TextDoc.txt";
						HearForFile.DirectoryListIP = clientIP;
						
					}
					else
					{
						Socket getFile = new Socket( message[0] , 10002 );
						
						DataOutputStream dataOutputStream = new DataOutputStream( getFile.getOutputStream()  );
						dataOutputStream.writeBytes( "TextDoc.txt- - - - " + "\n");
						
						BufferedReader bReader = new BufferedReader( new InputStreamReader( getFile.getInputStream() ) );
						BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter("TextDoc.txt") );
						
						String line = "";
						
						if( bReader.readLine().equals("yes") )
						{
							
							String text = "";
							while( !( line =  bReader.readLine() ).equals(")))")  )
							{
								text = text + line + "\n";
							}
							bufferedWriter.write(text);
							bufferedWriter.flush();
							bufferedWriter.close();
							
							Socket socketSend = new Socket( clientIP , 10002 );
							DataOutputStream dstrm = new DataOutputStream( socketSend.getOutputStream() );
							dstrm.writeBytes( "add-TextDoc.txt" + "\n" );
						    socketSend.close();
						    
						    Socket sendFile = new Socket( message[2] , 10001);
							BufferedReader bufferedReade = new BufferedReader( new FileReader( "TextDoc.txt" ) );
							DataOutputStream dpsrtm = new DataOutputStream( sendFile.getOutputStream() );
							String lin = "";
							while( ( lin = bufferedReade.readLine() ) != null )
							{
								dpsrtm.writeBytes( lin + "\n" );
							}
							dpsrtm.writeBytes( ")))" + "\n" );
							sendFile.close();
							
							HearForFile.fileName = "TextDoc.txt";
							HearForFile.DirectoryListIP = clientIP;
						    
							new DeleteOldFile( HearForFile.fileName  ).start();
							System.out.println("Delete Thread started");
							
						}
						else
						{
							Socket deleteFile = new Socket( clientIP , 10002 );
							DataOutputStream dstrm = new DataOutputStream( deleteFile.getOutputStream() );
							dstrm.writeBytes( message[0]+"-TextDoc.txt" + "\n" );
							deleteFile.close();
						}
					}
				}
			}
			else if( message.length == 2 )
			{
				System.out.println("entered to add");
				if(message[0].equals("add"))
				{
					boolean contained = false;
					for( int i = 0; i < directoryLists.size(); i++ )
					{
						if( directoryLists.get(i).FileName.equals(message[1]) )
						{
							directoryLists.get(i).IPList.add(clientIP);
							contained = true;
						}
					}
					if( contained == false )
					{
			            DirectoryList directoryList = new DirectoryList("TextDoc.txt");
			            directoryList.IPList.add(clientIP);
			            directoryLists.add(directoryList);
					}
					for( int j = 0; j < directoryLists.size(); j++ )
					{
						if( directoryLists.get(j).FileName.equals(message[1]) )
						{
							for( int k = 0; k < directoryLists.get(j).IPList.size(); k++ )
							{
								System.out.println(directoryLists.get(j).IPList.get(k));
							}
						}
					}
					System.out.println("------------------------------------------------");
				}
				else
				{
					for( int i = 0; i < directoryLists.size() ; i++ )
					{
						if( directoryLists.get(i).FileName.equals(message[1]) )
						{
							int size = directoryLists.get(i).IPList.size();
							int j = size - 1;
							while( j >= 0 )
							{
								if( directoryLists.get(i).IPList.get(j).equals(message[0]) )
								{
									directoryLists.get(i).IPList.remove(j);
									break;
								}
								j++;
							}
						}
					}
				}
			}
			
			else if( message.length == 5 )
			{
				System.out.println("entered");
				if( HearForFile.fileName.equals("") )
				{
					System.out.println("Entered2");
				}
				else
				{
					System.out.println("Entered3");
					DataOutputStream dataOutputStream = new DataOutputStream( socket.getOutputStream() );
					String line = "";
					dataOutputStream.writeBytes("yes" + "\n");
					File file = new File("TextDoc.txt");
					Scanner scanner = new Scanner( file );
					while( scanner.hasNextLine() ) 
					{
						System.out.println("Entered while");
						dataOutputStream.writeBytes( scanner.nextLine() + "\n" );
					}
					System.out.println( line );
					dataOutputStream.writeBytes( ")))" + "\n" );
				}
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
}

public class DirectoryListServer extends Thread
{
	ServerSocket listener;
	ArrayList<NeighbourPeer> neighbourPeers;
	ArrayList<Zone> belongedZones;
	String fileName = "";
	ArrayList<DirectoryList> directoryLists;
	
	public DirectoryListServer( ArrayList<NeighbourPeer> neighbourPeers , ArrayList<Zone> belongedZones )
	{
		try
		{
			listener = new ServerSocket(10002);
			this.neighbourPeers = neighbourPeers;
			this.belongedZones = belongedZones;
			System.out.println(belongedZones.size());
			this.directoryLists = new ArrayList<DirectoryList>();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while(true)
		{
			try
			{
				Socket socket = listener.accept();
				new DirectoryListServerResponse(neighbourPeers, belongedZones, fileName, socket , directoryLists).start();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
}

package experimental;

/*import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.github.fommil.emokit.Emotiv;
import com.github.fommil.emokit.EmotivListener;
import com.github.fommil.emokit.Packet;
import com.github.fommil.emokit.jpa.EmotivDatum;
import com.github.fommil.emokit.jpa.EmotivSession;*/

public class TestEmokitJava{

	/*public static void main(String[] args) throws Exception{
		Emotiv emotiv = new Emotiv();

		final EmotivSession session = new EmotivSession();
		session.setName("My Session");
		session.setNotes("My Notes for " + emotiv.getSerial());

		final Condition condition = new ReentrantLock().newCondition();

		emotiv.addEmotivListener(new EmotivListener(){
			@Override
			public void receivePacket(Packet packet){
				EmotivDatum datum = EmotivDatum.fromPacket(packet);
				datum.setSession(session);
				System.out.println(datum.toString());
			}

			@Override
			public void connectionBroken(){
				condition.signal();
			}
		});

		emotiv.start();
		condition.await();
	}*/

}

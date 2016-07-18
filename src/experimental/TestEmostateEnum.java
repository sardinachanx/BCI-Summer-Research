package experimental;

import com.emotiv.edk.EmoState;

public class TestEmostateEnum {
	public static void main(String[]args){
		EmoState.EE_ExpressivAlgo_t[] array = EmoState.EE_ExpressivAlgo_t.values();
		for(EmoState.EE_ExpressivAlgo_t o : array){
			System.out.println(o.ToInt());
		}
	}
}

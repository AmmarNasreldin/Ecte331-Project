package projectB;


class Data{
	
	int A1,A2,A3,B1,B2,B3;
	boolean goFunA1= false;
	boolean goFunA2= false;
	boolean goFunA3= false;
	boolean goFunB1= false;
	boolean goFunB2= false;
	boolean goFunB3= false;
	
}

public class TaskB {
	
	public static void  main(String[] args) throws InterruptedException {
			int Testing_Size = 4;
			int i;
			Data mySample = new Data();
			
			
			for (i=0; i< Testing_Size ; i++) {
				System.out.println("iteration "+ i );
				mySample.goFunA2 = false;
				mySample.goFunA3 = false;
				mySample.goFunB2 = false;
				mySample.goFunB3 = false;
				
				ThreadA a = new ThreadA(mySample);
				ThreadB b = new ThreadB(mySample);
				a.start();
				b.start();
				
				a.join();
				b.join();
			}
	}
}


class ThreadA extends Thread{
	private Data sample;
	
	public ThreadA(Data sample) {
		this.sample = sample ;
	}
	public void run() {
		synchronized(sample) {
			int n = 500 ;
			sample.A1 = n* (n+1)/2 ;
			System.out.println("A1 = "+ sample.A1);
			sample.goFunB2 = true;
			sample.notify();
		}
		synchronized(sample) {
			
			try {
				while (sample.goFunA2 == false) {
					sample.wait();
					System.out.println("Thread A2 waiting ");
				} 

				int n = 300;
				sample.A2 = sample.B2 + n*(n+1)/2;
				System.out.println("A2 = " + sample.A2);
				sample.goFunB3 = true;
				sample.notify();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		synchronized(sample) {
			try {
				while (sample.goFunA3 == false) {
					sample.wait();
					System.out.println("Thread A3 waiting ");
				} 
				int n = 400;
				sample.A3 = sample.B3 + n*(n+1)/2;
				System.out.println("A3 = " + sample.A3);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}





class ThreadB extends Thread {
	private Data sample;

	public ThreadB(Data sample) {
		super();
		this.sample = sample;
	}

	public void run(){

		synchronized(sample) {
			int n = 250;
			sample.B1 = n*(n+1)/2;
			System.out.println("B1 = " + sample.B1);
			sample.notify();
		}

		synchronized(sample) {
			try {
				while (sample.goFunB2 == false) {
					sample.wait();
					System.out.println("Thread B2 waiting ");
				} 

				int n = 200;
				sample.B2 = sample.A1 + n*(n+1)/2;
				System.out.println("B2 = " + sample.B2);
				sample.goFunA2 = true;
				sample.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		synchronized(sample) {
			try {
				while (sample.goFunB3 == false) {
					sample.wait();
					System.out.println("Thread B3 waiting ");
				} 

				int n = 400;
				sample.B3 = sample.A2 + n*(n+1)/2;
				System.out.println("B3 = " + sample.B3);
				sample.goFunA3 = true;
				sample.notify();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

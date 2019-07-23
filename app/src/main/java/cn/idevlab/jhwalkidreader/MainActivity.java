package cn.idevlab.jhwalkidreader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cunoraz.gifview.library.GifView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import cn.com.keshengxuanyi.mobilereader.NFCReaderHelper;
import cn.com.keshengxuanyi.mobilereader.UserInfo;

public class MainActivity extends AppCompatActivity {




    private static ImageView iv_zhaopian;
    static Handler uiHandler = null;


    //TODO:替换appSecret和appSecret
    private static String appKey = "950c89197f66";

    private static String appSecret = "2848501bc3bd";

    // 身份证头像解析服务器

    static String headip = "153.99.44.234";

    static String headipbak = "ds.jsske.com";

    static int headport = 9098;

    NfcAdapter mNfcAdapter;
    //TextView uuIdText;
    private NFCReaderHelper mNFCReaderHelper;
    PendingIntent mNfcPendingIntent;
    TextView tvname;
    TextView tvsex;
    TextView tvnation;
    TextView tvbirthday;
    TextView tvcode;
    TextView tvaddress;
    TextView tvdate;
    TextView tvdepar;
    TextView readerstatText;

    TextView tvshijiancontent;
    CardView cardView;
    private GifView gifView;
    Button buttonset;

    private Context context = null;

    /**
     * 是否本地解析头像
     */
    private Boolean isLocalParsingImage = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gifView= (GifView) findViewById(R.id.gifView);
        //设置图片源
        gifView.setGifResource(R.mipmap.nfc);

        //uuIdText = ((TextView) findViewById(R.id.tvuuid));
        iv_zhaopian = (ImageView) findViewById(R.id.ivHead);
        tvname = (TextView) findViewById(R.id.tvname2);
        tvsex = (TextView) findViewById(R.id.tvsex2);
        tvnation = (TextView) findViewById(R.id.tvnation2);
        tvbirthday = (TextView) findViewById(R.id.tvbirthday2);
        tvcode = (TextView) findViewById(R.id.tvcode2);
        tvaddress = (TextView) findViewById(R.id.tvaddress2);
        tvdate = (TextView) findViewById(R.id.tvdate2);
        tvdepar = (TextView) findViewById(R.id.tvdepart2);
        readerstatText = (TextView) findViewById(R.id.readerstatText);
        tvshijiancontent = (TextView) findViewById(R.id.tvshijiancontent);
        cardView = (CardView)findViewById(R.id.cardView);
        cardView.setRadius(8);//设置图片圆角的半径大小

        cardView.setCardElevation(8);//设置阴影部分大小

        context = this;
        uiHandler = new MyHandler(this);

        // 设备注册
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // 判断设备是否可用
        if (mNfcAdapter == null) {
            toast("该设备不支持nfc!");

            return;
        }

        if ((null != mNfcAdapter) && !mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
            finish();

            return;
        }

        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        mNFCReaderHelper = new NFCReaderHelper(this, uiHandler, appKey,
                appSecret, true);
    }

    @Override
    public void onResume() {
        super.onResume();

        try {
            if (null != mNfcPendingIntent) {
                mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                        null, null);
                resolvIntent(getIntent());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            // 0表示本地解析，1-表示网络解析
            isLocalParsingImage = !"1".equals(SpUtil.getString(context,
                    "touxiang_set", "0"));

        } catch (Exception ex) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO: A8POS 需要注释掉
        try {
            if (mNfcAdapter != null) {
                mNfcAdapter.disableForegroundDispatch(this);

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        try {
            setIntent(intent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    synchronized void resolvIntent(Intent intent) {
        try {
            String action = intent.getAction();

            if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                new NFCReadTask(intent, context).execute();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void toast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private class NFCReadTask extends AsyncTask<Void, Void, String> {
        private Intent mIntent = null;
        private Context context = null;
        private long beginTime;

        public NFCReadTask(Intent i, Context contextTemp) {
            mIntent = i;
            context = contextTemp;
        }

        @Override
        protected String doInBackground(Void... params) {

            beginTime = System.currentTimeMillis();

            String strCardInfo = mNFCReaderHelper.readCardWithIntent(mIntent);

            // 获取uuid
            String uuid = mNFCReaderHelper.readCardUUId(mIntent);

            return uuid + "," + strCardInfo;
        }

        @Override
        protected void onPostExecute(String strCardInfo) {
            super.onPostExecute(strCardInfo);

            String uuid = "";
            try {
                uuid = strCardInfo.split(",")[0];
                strCardInfo = strCardInfo.split(",")[1];
            } catch (Exception ex) {

            }

            //uuIdText.setText(uuid);
            tvshijiancontent.setText((System.currentTimeMillis() - beginTime)
                    + "毫秒");

            if ((null != strCardInfo) && (strCardInfo.length() > 1600)) {
                UserInfo userInfo = mNFCReaderHelper
                        .parsePersonInfoNew(strCardInfo);
                tvname.setText(userInfo.name);
                tvsex.setText(userInfo.sex);
                tvnation.setText(userInfo.nation);
                tvbirthday.setText(userInfo.brithday);
                tvcode.setText(userInfo.id);
                tvaddress.setText(userInfo.address);
                tvdate.setText(userInfo.exper + "-" + userInfo.exper2);
                tvdepar.setText(userInfo.issue);

                // TODO:
                if (isLocalParsingImage) {
                    // 本地动态库解析
//					Bitmap bm = mNFCReaderHelper.decodeImagexxxXT(strCardInfo);
                    Bitmap bm = mNFCReaderHelper.decodeImagexxxNewBit(strCardInfo);
                    iv_zhaopian.setImageBitmap(bm);
                } else {
                    // 网络解析头像
                    ShowHeadThread showThread = new ShowHeadThread();
                    showThread.img = mNFCReaderHelper
                            .decodeImageByte(strCardInfo);
                    showThread.start();
                }

            }
        }
    }

    static class ShowHeadThread extends Thread {
        static Handler handler = new Handler();
        byte[] img; // 记录用户头像

        private byte[] regulardata(byte[] sourbyte) {
            int len = sourbyte.length;
            byte[] desbyte = new byte[len + 8];
            String strbin = Integer.toBinaryString(len);

            int tmplen = strbin.length();

            for (int i = 0; i < (16 - tmplen); i++) {
                strbin = "0" + strbin;
            }

            String tmp1 = strbin.substring(0, 8);
            desbyte[4] = (byte) ((int) Integer.valueOf(tmp1, 2));
            tmp1 = strbin.substring(8, 16);
            desbyte[5] = (byte) ((int) Integer.valueOf(tmp1, 2));
            desbyte[0] = (byte) 0xff;
            desbyte[1] = 3;
            desbyte[2] = 5;
            desbyte[3] = 0;
            desbyte[len - 2] = 0;
            desbyte[len - 1] = (byte) 0xaa;
            System.arraycopy(sourbyte, 0, desbyte, 6, sourbyte.length);

            return desbyte;
        }

        @Override
        public synchronized void run() {
            DatagramPacket packet = null;
            DatagramSocket udpSocket = null;
            DatagramPacket packet2 = null;

            byte[] tmp = new byte[5000];
            for (int i = 0; i < 100; i++) {
                tmp[i] = 0x00;
            }
            byte[] lastimg = null;

            try {
                byte[] img2 = regulardata(img);

                packet = new DatagramPacket(img2, img2.length,
                        InetAddress.getByName(headip), headport);
                packet.setData(img2, 0, img2.length);

                udpSocket = new DatagramSocket();
                udpSocket.send(packet);

                try {
                    packet2 = new DatagramPacket(tmp, tmp.length);
                    udpSocket.setSoTimeout(1500);
                    udpSocket.receive(packet2);
                    lastimg = new byte[packet2.getLength() - 8];
                    System.arraycopy(tmp, 6, lastimg, 0, lastimg.length);
                    data2view(lastimg);
                } catch (SocketTimeoutException ee) {
                    ee.printStackTrace();

                    // 如果接收超时，则再发
                    try {
                        packet = new DatagramPacket(img2, img2.length,
                                InetAddress.getByName(headipbak), headport);
                        packet.setData(img2, 0, img2.length);

                        udpSocket = new DatagramSocket();
                        udpSocket.send(packet);
                        packet2 = new DatagramPacket(tmp, tmp.length);
                        udpSocket.setSoTimeout(1500);
                        udpSocket.receive(packet2);
                        lastimg = new byte[packet2.getLength() - 8];
                        System.arraycopy(tmp, 6, lastimg, 0, lastimg.length);
                        data2view(lastimg);
                    } catch (SocketTimeoutException see) {
                        see.printStackTrace();
                    }
                }
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        }

        void data2view(final byte[] imgByte) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte,
                                0, imgByte.length);
                        iv_zhaopian.setImageBitmap(bitmap);
                    } catch (Exception ee) {
                    }
                }
            });
        }
    }

    class MyHandler extends Handler {
        private MainActivity activity;

        MyHandler(MainActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:

                    String msgTemp = (String) msg.obj;
                    readerstatText.setText(msgTemp);

                    break;
            }
        }
    }
}

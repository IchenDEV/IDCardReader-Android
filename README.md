# JHWalkIDReader

## NFC SDK集成说明

### NFC SDK集成步骤
集成之前请先试用提供的demo进行测试，测试OK后再进行集成。集成步骤如下：
#### AndroidManifest.xml中增加NFC权限
AndroidManifest.xml中增加NFC权限和读写权限。说明：使用读写权限是由于身份证图片解析到sd卡。
  <uses-permission android:name="android.permission.NFC" />
  <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
  <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
  <uses-permission android:name="android.permission.READ_PHONE_STATE" />

#### 集成jar包和so文件
1、集成ksxy.jar和org.apache.http.legacy.jar两个jar包。
2、集成libwlt2bmp.so、libWltRS.so文件。该文件是头像转换使用。

#### 增加读取身份证Activity
增加读取身份证Activity文件，参照demo中MainActivity.java文件。主要步骤包括：江苏科盛轩逸有限公司
onCreate方法增加NFC操作和授权码初始化校验
其中，uiHandler用于展示身份证读卡进度。mNFCReaderHelper用于授权码校验，只有校验通过才能读取身份证信息。
onResume()方法增加NFC操作
说明：NFCReadTask为读取身份证。

#### 解析身份证信息

  Bitmap bm = mNFCReaderHelper.decodeImagexxxNewBit(strCardInfo);

头像解析分为本地和网络。建议使用本地解析（即使用.so文件解析）。如果本地解析不了，再使用网络解析。
说明
如果是LANDI_APOS A8 设备，需要将onPause方法里的操作注释掉。
  //try {
  //	if (mNfcAdapter != null) {
  //		mNfcAdapter.disableForegroundDispatch(this);
  //
  //			}
  //		} catch (Exception ex) {
  //			ex.printStackTrace();
  //		}

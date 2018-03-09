# InternationalPhoneInput
类似微信注册时的国际手机号输入控件

![image](https://github.com/yimingyu/InternationalPhoneInput/blob/master/ScreenShots/Screenshot_2016-09-10-21-54-06.png)
![image](https://github.com/yimingyu/InternationalPhoneInput/blob/master/ScreenShots/Screenshot_2016-09-10-20-52-29.png)


图片资源和部分代码参考了 [IntlPhoneInput](https://github.com/AlmogBaku/IntlPhoneInput)

主要修改：  
1、修复了IntlPhoneInput中初始时如果获取到手机号则功能失常的Bug。  
2、修得了因手机系统语言更改引起的相关BUG。    
3、选择国家和区号的形式从原IntlPhoneInput中Spinner改成Activity,实现方法是在View的onAttachedToWindow方法中注册广播接收器接收CountrySelectorActivity发送的结果。    
需要改进： 
1、当dialCode超过3位如美国(+1684)时，输入14789568706，控件是检查号码 +1   4789568706的合格性，而不是+1684 14789568706，内部控件这样做的原因还有待调查。     
2、界面的美化，以及考虑是否用中文表示所有国家和地区的名称。     
使用注意：  
1、由于控件内部的Activity继承于AppCompatActivity并且没有指定theme，使用时App的Theme一定要继承于AppCompatTheme。    
2、由于项目需要，控件内置getUstr()方法获取+86-14789568706这种格式的手机号。







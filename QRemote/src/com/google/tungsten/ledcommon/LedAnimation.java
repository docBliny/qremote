// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.google.tungsten.ledcommon;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;

public class LedAnimation
{
    private ArrayList<LED> mLedList;
    private int[] mProgram;
    
    public static class LED
    {

        public int b;
        public int g;
        public int id;
        public int r;
        public int startMs;

        public LED(int id, int r, int g, int b, int startMS)
        {
            this.id = id;
            this.r = r;
            this.g = g;
            this.b = b;
            startMs = startMS;
        }
    }


    public LedAnimation(int size)
    {
        mLedList = new ArrayList<LED>(size);
    }

    public LedAnimation(int[] ai)
    {
        mProgram = ai;
    }

    public void addLed(LED led)
    {
        if(mLedList == null)
        {
            throw new IllegalStateException("cannot add LEDs to statically defined animations");
        } else
        {
            mLedList.add(led);
            return;
        }
    }

    public void writeToParcel(Parcel parcel, int i)
    {
        if(mLedList != null)
        {
            int j = mLedList.size();
            Log.d("aah.LedAnimation", (new StringBuilder()).append("writeToParcel size: ").append(j).toString());
            parcel.writeInt(j * 5);
            for(int k = 0; k < j; k++)
            {
                LED led = (LED)mLedList.get(k);
                parcel.writeInt(led.id);
                parcel.writeInt(led.r);
                parcel.writeInt(led.g);
                parcel.writeInt(led.b);
                parcel.writeInt(led.startMs);
            }

        } else
        {
            parcel.writeIntArray(mProgram);
        }
    }

    public static final Creator<LedAnimation> CREATOR = new Creator<LedAnimation>() {

        public LedAnimation createFromParcel(Parcel parcel)
        {
            return new LedAnimation(parcel.createIntArray());
        }

        public LedAnimation[] newArray(int size)
        {
            return new LedAnimation[size];
        }

    };


}

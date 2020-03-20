package confluent.io;

public class ReverseString
{
    public static String process(String input)
    {
        // getBytes() method to convert string
        // into bytes[].
        byte [] strAsByteArray = input.getBytes();

        byte [] result =
                new byte [strAsByteArray.length];

        // Store result in reverse order into the
        // result byte[]
        for (int i = 0; i<strAsByteArray.length; i++)
            result[i] =
                    strAsByteArray[strAsByteArray.length-i-1];

        String s = new String(result);

        return s;

    }
}
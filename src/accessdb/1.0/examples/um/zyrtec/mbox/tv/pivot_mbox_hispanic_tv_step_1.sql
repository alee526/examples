SELECT [Time_Lookup_Day to Week Ending Sunday].[Week Ending Sunday], [Mbox Hispanic TV tbl].BRND, [Mbox Hispanic TV tbl].[NETWORK NAME], [DDS_Lookup TV Time Length].Len, [Mbox Hispanic TV tbl].DAYPART, Sum([Mbox Hispanic TV tbl].HH) AS SumOfHH, Sum([Mbox Hispanic TV tbl].WM2554) AS SumOfWM2554, Sum([Mbox Hispanic TV tbl].Ordered) AS SumOfOrdered FROM ([Time_Lookup_Day to Week Ending Sunday] LEFT JOIN [Mbox Hispanic TV tbl] ON [Time_Lookup_Day to Week Ending Sunday].Date = [Mbox Hispanic TV tbl].DATE) LEFT JOIN [DDS_Lookup TV Time Length] ON [Mbox Hispanic TV tbl].LEN = [DDS_Lookup TV Time Length].[Mbox TV Time] GROUP BY [Time_Lookup_Day to Week Ending Sunday].[Week Ending Sunday], [Mbox Hispanic TV tbl].BRND, [Mbox Hispanic TV tbl].[NETWORK NAME], [DDS_Lookup TV Time Length].Len, [Mbox Hispanic TV tbl].DAYPART;


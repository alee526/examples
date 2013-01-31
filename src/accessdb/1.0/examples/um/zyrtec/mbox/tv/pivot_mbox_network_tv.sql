SELECT [DDS_Lookup Monday to Sunday].[Week Ending Sunday], [Mbox Network TV tbl].Date, [Mbox Network TV tbl].Client, [Mbox Network TV tbl].Brand, [Mbox Network TV tbl].N, [Mbox Network TV tbl].[Type Net], [Mbox Network TV tbl].Daypart, [Mbox Network TV tbl].Network, [Mbox Network TV tbl].[HH GRP], [Mbox Network TV tbl].[WM2554 GRP], [Mbox Network TV tbl].Ordered, [DDS_Lookup Brand Name].[Datacube Product Name], [DDS_Lookup TV Time Length].Len, [Master Daypart_Lookup_tbl].[Day Part] FROM ((([DDS_Lookup Monday to Sunday] LEFT JOIN [Mbox Network TV tbl] ON [DDS_Lookup Monday to Sunday].[Week Starting Monday] = [Mbox Network TV tbl].[Week of]) LEFT JOIN [DDS_Lookup Brand Name] ON [Mbox Network TV tbl].Brand = [DDS_Lookup Brand Name].Prd) LEFT JOIN [DDS_Lookup TV Time Length] ON [Mbox Network TV tbl].LEN = [DDS_Lookup TV Time Length].[Mbox TV Time]) LEFT JOIN [Master Daypart_Lookup_tbl] ON [Mbox Network TV tbl].Daypart = [Master Daypart_Lookup_tbl].[Mbox Daypart];

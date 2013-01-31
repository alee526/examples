SELECT [DDS_Lookup Monday to Sunday].[Week Ending Sunday], [Master Daypart_Lookup_tbl].[Day Part], [DDS TV Daypart tbl].[ACT WM3564 GRP], [DDS TV Daypart tbl].[ACT WM2554 GRP], [DDS TV Daypart tbl].[ACT HOMES GRP], [DDS TV Daypart tbl].Spend, [DDS TV Daypart tbl].Prd FROM ([DDS_Lookup Monday to Sunday] LEFT JOIN [DDS TV Daypart tbl] ON [DDS_Lookup Monday to Sunday].[Week Starting Monday] = [DDS TV Daypart tbl].[Week Starting Monday]) LEFT JOIN [Master Daypart_Lookup_tbl] ON [DDS TV Daypart tbl].DAYPART = [Master Daypart_Lookup_tbl].[Mbox Daypart];

SELECT [DDS_Lookup Monday to Sunday].[Week Ending Sunday], [DDS Spot TV tbl].Media, [DDS Spot TV tbl].Client, [DDS Spot TV tbl].Product, [DDS Spot TV tbl].Estimate, [DDS Spot TV tbl].Market, [DDS Spot TV tbl].Station, [DDS Spot TV tbl].Times, [DDS Spot TV tbl].Daypart, [DDS Spot TV tbl].Len, [DDS Spot TV tbl].Program, [DDS Spot TV tbl].Film, [DDS Spot TV tbl].[PURCH RWM2554], [DDS Spot TV tbl].[PURCH RHOMES], [DDS Spot TV tbl].[PURCH DOLLARS], [DDS Spot TV tbl]![PURCH RHOMES]*[DMA to IRI Nat+Region wgts]![National Weight] AS [Normalized HH GRP], [DMA to IRI Nat+Region wgts].[Classification Type] FROM ([DDS_Lookup Monday to Sunday] LEFT JOIN [DDS Spot TV tbl] ON [DDS_Lookup Monday to Sunday].[Week Starting Monday] = [DDS Spot TV tbl].Week) LEFT JOIN [DMA to IRI Nat+Region wgts] ON [DDS Spot TV tbl].Market = [DMA to IRI Nat+Region wgts].[DDS Spot TV Market];
SELECT [Time Dimension tbl].[Week Ending Sunday], [Pivot Kantar TV Step1].PARENT, [Pivot Kantar TV Step1].PRODUCT, [Pivot Kantar TV Step1].[TIME PERIOD], [Pivot Kantar TV Step1].PROPERTY, [Pivot Kantar TV Step1].DAYPART, [Pivot Kantar TV Step1].Len, [Pivot Kantar TV Step1].[TV CREATIVE], [Pivot Kantar TV Step1].[NETWORK TV W 25-54 GRP], [Pivot Kantar TV Step1].[SPOT TV W 25-54 GRP], [Pivot Kantar TV Step1].[SLN TV W 25-54 GRP], [Pivot Kantar TV Step1].[CABLE TV W 25-54 GRP], [Pivot Kantar TV Step1].[SYNDICATION W 25-54 GRP], [Pivot Kantar TV Step1].[NETWORK TV HH GRP], [Pivot Kantar TV Step1]![SPOT TV HH GRP]*[Pivot Kantar TV Step1]![National Weight] AS [SPOT TV HH GRP], [Pivot Kantar TV Step1].[SLN TV HH GRP], [Pivot Kantar TV Step1].[CABLE TV HH GRP], [Pivot Kantar TV Step1].[SYNDICATION HH GRP], [Pivot Kantar TV Step1].Brand, [Pivot Kantar TV Step1].Length, [Pivot Kantar TV Step1].[Day Part], [Pivot Kantar TV Step1].[SPOT TV HH GRP] AS [ORG SPOT TV HH GRP], [Pivot Kantar TV Step1].[NETWORK TV DOLS (000)], [Pivot Kantar TV Step1].[SPOT TV DOLS (000)], [Pivot Kantar TV Step1].[SLN TV DOLS (000)], [Pivot Kantar TV Step1].[CABLE TV DOLS (000)], [Pivot Kantar TV Step1].[SYNDICATION DOLS (000)], ([Pivot Kantar TV Step1]![NETWORK TV DOLS (000)]+[Pivot Kantar TV Step1]![SPOT TV DOLS (000)]+[Pivot Kantar TV Step1]![SLN TV DOLS (000)]+[Pivot Kantar TV Step1]![CABLE TV DOLS (000)]+[Pivot Kantar TV Step1]![SYNDICATION DOLS (000)])*1000 AS [Media Spend], [Pivot Kantar TV Step1].[Market]  FROM [Time Dimension tbl] LEFT JOIN [Pivot Kantar TV Step1] ON [Time Dimension tbl].Date = [Pivot Kantar TV Step1].[Week Ending Sunday];

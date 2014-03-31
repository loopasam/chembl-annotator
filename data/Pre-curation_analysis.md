# Pre-curation charaterization of automated annotation process

- Annotation process takes 1240 minutes (20.6 hours on crunch) over ChEMBL 17

_Number of annotated assays:_

`SELECT COUNT(DISTINCT id) FROM AnnotatedAssay`
> 364,857

_Number of assays:_

`SELECT COUNT(DISTINCT assay_id) FROM assays`
> 734,201

_Percent of assays annotated:_

> 49.7%

# Distribution of terms that have been used to annotate assays

SELECT BaoTerm.label, count(AnnotatedAssay.id) as pound
FROM AnnotatedAssay
INNER JOIN Annotation ON AnnotatedAssay.id = Annotation.assay_id
INNER JOIN BaoTerm ON Annotation.term_id = BaoTerm.id
GROUP BY BaoTerm.label
ORDER BY pound DESC



>= 5
'279365'

<5
'139874'


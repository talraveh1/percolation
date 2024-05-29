-- calculate the average stretch per PN to nodes on the axis in fixed distance, show count too
select cast(PN as float) / 10.0                                                              as PROB,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1, 4) as STRETCH,
       sum(DSUM)                                                                             as DSUM,
       sum(DCNT)                                                                             as DCNT
from RES
where ((X = 0 and abs(Y) = 4096) or (abs(X) = 4096 and Y = 0))
group by PN
order by PN;

-- calculate the average stretch per PN to nodes in angle 45 degrees, in fixed distance, show count too
select cast(PN as float) / 10.0                                                    as PROB,
       cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1 as STRETCH,
       sum(DSUM)                                                                   as DSUM,
       sum(DCNT)                                                                   as DCNT
from RES
where abs(X) + abs(Y) = 4096 and abs(X) = abs(Y) and mod(PN, 5) = 0
group by PN
order by PN;

-- calculate the average stretch per PN to nodes on the axis in fixed distance, column per distance
select cast(PN as float) / 10.0 as PROB,
       round(cast(sum(case when abs(X) = 4096 or abs(Y) = 4096 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 4096 or abs(Y) = 4096 then DCNT else 0 end) as float), 0) / 4096.0 - 1,
             4)                 as STRETCH_4096,
       round(cast(sum(case when abs(X) = 2048 or abs(Y) = 2048 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 2048 or abs(Y) = 2048 then DCNT else 0 end) as float), 0) / 2048.0 - 1,
             4)                 as STRETCH_2048,
       round(cast(sum(case when abs(X) = 1024 or abs(Y) = 1024 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 1024 or abs(Y) = 1024 then DCNT else 0 end) as float), 0) / 1024.0 - 1,
             4)                 as STRETCH_1024,
       round(cast(sum(case when abs(X) = 512 or abs(Y) = 512 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 512 or abs(Y) = 512 then DCNT else 0 end) as float), 0) / 512.0 - 1,
             4)                 as STRETCH_512,
       round(cast(sum(case when abs(X) = 256 or abs(Y) = 256 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 256 or abs(Y) = 256 then DCNT else 0 end) as float), 0) / 256.0 - 1,
             4)                 as STRETCH_256,
       round(cast(sum(case when abs(X) = 128 or abs(Y) = 128 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 128 or abs(Y) = 128 then DCNT else 0 end) as float), 0) / 128.0 - 1,
             4)                 as STRETCH_128,
       round(cast(sum(case when abs(X) = 64 or abs(Y) = 64 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 64 or abs(Y) = 64 then DCNT else 0 end) as float), 0) / 64.0 - 1,
             4)                 as STRETCH_64,
       round(cast(sum(case when abs(X) = 32 or abs(Y) = 32 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 32 or abs(Y) = 32 then DCNT else 0 end) as float), 0) / 32.0 - 1,
             4)                 as STRETCH_32,
       round(cast(sum(case when abs(X) = 16 or abs(Y) = 16 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 16 or abs(Y) = 16 then DCNT else 0 end) as float), 0) / 16.0 - 1,
             4)                 as STRETCH_16,
       round(cast(sum(case when abs(X) = 8 or abs(Y) = 8 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 8 or abs(Y) = 8 then DCNT else 0 end) as float), 0) / 8.0 - 1,
             4)                 as STRETCH_8,
       round(cast(sum(case when abs(X) = 4 or abs(Y) = 4 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 4 or abs(Y) = 4 then DCNT else 0 end) as float), 0) / 4.0 - 1,
             4)                 as STRETCH_4,
       round(cast(sum(case when abs(X) = 2 or abs(Y) = 2 then DSUM else 0 end) as float) /
             nullif(cast(sum(case when abs(X) = 2 or abs(Y) = 2 then DCNT else 0 end) as float), 0) / 2.0 - 1,
             4)                 as STRETCH_2
from RES
where ((X = 0 and abs(Y) in (2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096))
    or (abs(X) in (2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096) and Y = 0))
group by PN
order by PN;

-- calculate the average stretch per PN to nodes on the X (change for Y) axis in fixed distance
select PROB,
       DIST,
       round(DIST / 9557.0 - 1, 4) as STRETCH
from (select cast(PN as float) / 10.0                                                 as PROB,
             round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0), 4) as DIST
      from RES
      where X = 0
        and abs(Y) = 4096
        and mod(PN, 5) = 0 -- ensure PN is a multiple of 5
      group by PN);

-- calculate the average stretch per PN to nodes on the X axis in fixed distance
select cast(PN as float) / 10.0                                                              as PROB,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1, 4) as STRETCH
from RES
where X = 0
  and Y = 4096
  and mod(PN, 5) = 0 -- ensure PN is a multiple of 5
group by PN;

-- calculate the average stretch in respect to the distance for nodes on the same axis for a fixed value of p
select DIST                                                                                as OPTIMAL_DIST,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / DIST - 1, 3) as STRETCH,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0), 2)            as ACTUAL_DIST,
       sum(DCNT) as COUNT
from (select PN, X, Y, abs(X) + abs(Y) as DIST, DSUM, DCNT from RES)
where (X = 0 or Y = 0) and PN = 900
group by DIST
order by DIST;

-- same, for 45 deg.
select DIST                                                                                as OPTIMAL_DIST,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / DIST - 1, 3) as STRETCH,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0), 2)            as ACTUAL_DIST,
       sum(DCNT) as COUNT
from (select PN, X, Y, abs(X) + abs(Y) as DIST, DSUM, DCNT from RES)
where X = Y and PN = 750
group by DIST
order by DIST;

-- calculate the average stretch from 0,0 to every point in distance 4096 (from 0,0) for each PN
-- output the angle, the average stretch for each PN

-- drop the table if it exists
drop table if exists TMP;

-- create the temporary table
create table TMP
(
    PN      int,
    ANGLE   float,
    STRETCH float
);

-- insert records into the temporary table

select PN / 10                                                                               as PROB,
       round((180.0 / pi()) * atan2(abs(X), abs(Y)), 0)                                      as ANGLE,
       round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1, 4) as STRETCH
from RES
where abs(X) + abs(Y) = 4096
  and PN >= 500
group by PN, X, Y
order by PN, ANGLE;

select PROB,
       ANGLE,
       round(avg(STRETCH), 4) as STRETCH
from (select PN / 10                                                                               as PROB,
             round(180.0 * atan2(X, Y) / pi(), 0)                                                  as ANGLE,
             round(cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1, 2) as STRETCH
      from (select PN, abs(X) as X, abs(Y) as Y, DSUM, DCNT
            from RES
            where abs(X) + abs(Y) = 4096
              and PN >= 500
              and mod(PN, 10) = 0) as RES
      group by PN, X, Y)
group by PROB, ANGLE
order by PROB, ANGLE;

select
       ANGLE,
       avg(STRETCH) as STRETCH
from (select PN / 10                                                                     as PROB,
             round(180.0 * atan2(X, Y) / pi(), 0)                                        as ANGLE,
             cast(sum(DSUM) as float) / nullif(cast(sum(DCNT) as float), 0) / 4096.0 - 1 as STRETCH
      from (select PN, X, Y, DSUM, DCNT
            from RES
            where abs(X) + abs(Y) = 4096
              and PN = 550
              and mod(PN, 10) = 0) as RES
      group by PN, X, Y)
group by PROB, ANGLE
order by PROB, ANGLE;


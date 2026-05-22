-- Seed: Faculties
INSERT INTO faculties (code, name_th, name_en) VALUES
    ('SCI',  'คณะวิทยาศาสตร์และเทคโนโลยี',    'Faculty of Science and Technology'),
    ('BUS',  'คณะบริหารธุรกิจ',               'Faculty of Business Administration'),
    ('ENG',  'คณะวิศวกรรมศาสตร์',             'Faculty of Engineering'),
    ('LAW',  'คณะนิติศาสตร์',                 'Faculty of Law');

-- Seed: Departments (faculty_id references above)
INSERT INTO departments (faculty_id, code, name_th, name_en) VALUES
    (1, 'CS',   'ภาควิชาวิทยาการคอมพิวเตอร์',        'Department of Computer Science'),
    (1, 'SE',   'ภาควิชาวิศวกรรมซอฟต์แวร์',          'Department of Software Engineering'),
    (1, 'DS',   'ภาควิชาวิทยาการข้อมูล',             'Department of Data Science'),
    (2, 'MKT',  'ภาควิชาการตลาด',                   'Department of Marketing'),
    (2, 'FIN',  'ภาควิชาการเงินและการธนาคาร',         'Department of Finance and Banking'),
    (2, 'HRM',  'ภาควิชาการบริหารทรัพยากรมนุษย์',     'Department of Human Resource Management'),
    (3, 'CE',   'ภาควิชาวิศวกรรมโยธา',              'Department of Civil Engineering'),
    (3, 'EE',   'ภาควิชาวิศวกรรมไฟฟ้า',             'Department of Electrical Engineering'),
    (4, 'LAW1', 'สาขากฎหมายมหาชน',                  'Public Law Division'),
    (4, 'LAW2', 'สาขากฎหมายเอกชน',                  'Private Law Division');

export enum UploadAcceptEnum {
    excel = '.xlsx,.xls',
    word = '.docx,.doc',
    pdf = '.pdf',
    ppt = '.pptx,.ppt',
    txt = '.txt',
    plain = '.plain',
    video = '.mp4',
    sql = '.sql',
    csv = '.csv',
    zip = '.zip',
    xmind = '.xmind',
    image = '.jpg,.jpeg,.png,.svg,.webp,.gif,.bmp,.ico',
    jar = '.jar',
    sketch = '.sketch',
    none = 'none',
    unknown = 'unknown',
    json = '.json',
    jmx = '.jmx',
    har = '.har',
}

export enum UploadStatus {
    pending = 'pending',
    finished = 'finished',
    error = 'error',
    uploading = 'uploading',
    removed = 'removed',
    init = 'init',
}
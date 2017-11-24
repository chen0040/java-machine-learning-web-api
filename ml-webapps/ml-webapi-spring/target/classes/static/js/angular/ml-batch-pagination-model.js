var BatchPaginationModel = function () {
  this.currentPage = 1;
  this.pageSize = 30;
  this.batchSize = 175;
  this.pageSizes = [10, 30, 50, 100, 200, 500, 1000];
};

BatchPaginationModel.prototype.updatePageSize = function(){
  this.numPages = parseInt(Math.ceil(this.batchSize / this.pageSize), 10);
  this.currentPage = this.currentPage > this.numPages ? this.numPages : this.currentPage;
};

 BatchPaginationModel.prototype.updatePage = function(batch, callback) {


    var pageStart = (this.currentPage-1) * this.pageSize;
    var pageEnd = (this.currentPage) * this.pageSize;
    var batchPage = [];
    for(var i=pageStart; i < pageEnd; ++i){
        if(i < batch.tuples.length){
            batchPage.push(batch.tuples[i]);
        }
    }
    callback(batchPage);
};
